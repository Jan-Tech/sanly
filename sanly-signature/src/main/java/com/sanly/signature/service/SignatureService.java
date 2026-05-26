package com.sanly.signature.service;

import com.sanly.signature.client.BridgeClient;
import com.sanly.signature.client.NotificationClient;
import com.sanly.signature.client.RegistryClient;
import com.sanly.signature.dto.response.PageResponse;
import com.sanly.signature.dto.response.SignatureResponse;
import com.sanly.signature.dto.response.SignatureStatsResponse;
import com.sanly.signature.dto.response.VerificationLogResponse;
import com.sanly.signature.entity.SignatureRecord;
import com.sanly.signature.entity.SignatureStatus;
import com.sanly.signature.repository.SignatureRecordRepository;
import com.sanly.signature.repository.SignatureVerificationLogRepository;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.security.MessageDigest;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SignatureService {

    private static final Logger log = LoggerFactory.getLogger(SignatureService.class);

    private final SignatureRecordRepository signatureRecordRepository;
    private final SignatureVerificationLogRepository verificationLogRepository;
    private final SignatureCodeService signatureCodeService;
    private final HmacService hmacService;
    private final RegistryClient registryClient;
    private final BridgeClient bridgeClient;
    private final NotificationClient notificationClient;

    /** Per-citizen rate limit buckets: 10 signing requests per hour (not a Spring bean — initialised here). */
    private final ConcurrentHashMap<String, Bucket> citizenBuckets = new ConcurrentHashMap<>();

    // ─── Rate Limiting ────────────────────────────────────────────────────────

    private void checkRateLimit(String nationalId) {
        Bucket bucket = citizenBuckets.computeIfAbsent(nationalId, k ->
                Bucket.builder()
                        .addLimit(Bandwidth.classic(10, Refill.intervally(10, Duration.ofHours(1))))
                        .build());
        if (!bucket.tryConsume(1)) {
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS,
                    "Signing rate limit exceeded — maximum 10 signatures per hour");
        }
    }

    // ─── Sign ─────────────────────────────────────────────────────────────────

    /**
     * Signs a document on behalf of a citizen.
     *
     * Flow:
     *  1. Per-citizen rate check (10/hour)
     *  2. OTP verification via citizen-registry
     *  3. SHA-256 hash of the uploaded file
     *  4. Unique TM-SIG code generation (sequential, locked)
     *  5. HMAC-SHA256 signature of hash|nationalId|signedAt
     *  6. Persist SignatureRecord
     *  7. Async: publish to bridge + send notification
     */
    @Transactional
    public SignatureResponse sign(MultipartFile file,
                                  String purpose,
                                  String otpCode,
                                  String signerNationalId,
                                  String ipAddress,
                                  String language) {
        // 1. Per-citizen rate limit
        checkRateLimit(signerNationalId);

        // 2. OTP verification
        boolean otpValid = registryClient.verifyActionOtp(signerNationalId, otpCode);
        if (!otpValid) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Invalid or expired OTP — please request a new one");
        }

        // 3. SHA-256 hash of document bytes
        String documentHash = computeSha256(file);

        // 4. Generate unique signature code
        String signatureCode = signatureCodeService.generateCode();

        // 5. Compute HMAC-SHA256
        LocalDateTime signedAt = LocalDateTime.now();
        String signatureValue = hmacService.computeSignature(documentHash, signerNationalId, signedAt);

        // 6. Persist
        SignatureRecord record = new SignatureRecord();
        record.setSignatureCode(signatureCode);
        record.setSignerNationalId(signerNationalId);
        record.setDocumentHash(documentHash);
        record.setDocumentName(file.getOriginalFilename());
        record.setDocumentSizeBytes(file.getSize());
        record.setSignatureValue(signatureValue);
        record.setPurpose(purpose);
        record.setSignedAt(signedAt);
        record.setStatus(SignatureStatus.VALID);
        record.setIpAddress(ipAddress);
        SignatureRecord saved = signatureRecordRepository.save(record);

        log.info("Document signed: signatureCode={} NIN={}", signatureCode, signerNationalId);

        // 7. Async side effects (after the transaction commits, Spring will call these
        //    in the executor thread — they do NOT participate in this transaction)
        bridgeClient.publishSignature(signatureCode, signerNationalId, signedAt, purpose,
                SignatureStatus.VALID.name());
        notificationClient.notifyDocumentSigned(signerNationalId, signatureCode, purpose, language);

        return SignatureResponse.from(saved);
    }

    // ─── Queries ──────────────────────────────────────────────────────────────

    public PageResponse<SignatureResponse> getMySignatures(String nationalId, int page, int size) {
        var pageResult = signatureRecordRepository
                .findBySignerNationalIdOrderBySignedAtDesc(nationalId, PageRequest.of(page, size));
        return PageResponse.from(pageResult, SignatureResponse::from);
    }

    public SignatureResponse getByCode(String signatureCode, String nationalId, boolean isAdmin) {
        SignatureRecord record = findByCodeOrThrow(signatureCode);
        if (!isAdmin && !record.getSignerNationalId().equals(nationalId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "You are not authorised to view this signature");
        }
        SignatureResponse response = SignatureResponse.from(record);

        // Include verification log history
        List<VerificationLogResponse> logs = verificationLogRepository
                .findBySignatureCodeOrderByVerifiedAtDesc(signatureCode)
                .stream()
                .map(VerificationLogResponse::from)
                .collect(Collectors.toList());
        response.setVerificationLogs(logs);

        return response;
    }

    // ─── Revoke ───────────────────────────────────────────────────────────────

    @Transactional
    public SignatureResponse revoke(String signatureCode, String reason,
                                    String nationalId, boolean isAdmin) {
        SignatureRecord record = findByCodeOrThrow(signatureCode);

        if (!isAdmin && !record.getSignerNationalId().equals(nationalId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "You are not authorised to revoke this signature");
        }
        if (record.getStatus() == SignatureStatus.REVOKED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Signature has already been revoked");
        }

        record.setStatus(SignatureStatus.REVOKED);
        record.setRevokedAt(LocalDateTime.now());
        record.setRevokedReason(reason);
        SignatureRecord saved = signatureRecordRepository.save(record);

        log.info("Signature revoked: signatureCode={} by NIN={} isAdmin={}", signatureCode, nationalId, isAdmin);

        // Async notifications
        notificationClient.notifySignatureRevoked(
                record.getSignerNationalId(), signatureCode, reason, "tk");

        return SignatureResponse.from(saved);
    }

    // ─── Admin ────────────────────────────────────────────────────────────────

    public PageResponse<SignatureResponse> adminGetAll(int page, int size) {
        var pageResult = signatureRecordRepository
                .findAllByOrderBySignedAtDesc(PageRequest.of(page, size));
        return PageResponse.from(pageResult, SignatureResponse::from);
    }

    public SignatureStatsResponse getStats() {
        LocalDateTime startOfDay = LocalDateTime.now().toLocalDate().atStartOfDay();
        return SignatureStatsResponse.builder()
                .signedToday(signatureRecordRepository.countBySignedAtAfter(startOfDay))
                .verifiedToday(verificationLogRepository.countByVerifiedAtAfter(startOfDay))
                .revokedTotal(signatureRecordRepository.countByStatus(SignatureStatus.REVOKED))
                .totalSignatures(signatureRecordRepository.count())
                .build();
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private SignatureRecord findByCodeOrThrow(String signatureCode) {
        return signatureRecordRepository.findBySignatureCode(signatureCode)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Signature not found: " + signatureCode));
    }

    private String computeSha256(MultipartFile file) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(file.getBytes());
            StringBuilder sb = new StringBuilder(hash.length * 2);
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Failed to compute document hash");
        }
    }
}
