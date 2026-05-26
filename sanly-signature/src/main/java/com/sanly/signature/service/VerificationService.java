package com.sanly.signature.service;

import com.sanly.signature.dto.response.VerifyResponse;
import com.sanly.signature.entity.SignatureRecord;
import com.sanly.signature.entity.SignatureStatus;
import com.sanly.signature.entity.SignatureVerificationLog;
import com.sanly.signature.entity.VerificationResult;
import com.sanly.signature.repository.SignatureRecordRepository;
import com.sanly.signature.repository.SignatureVerificationLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.security.MessageDigest;
import java.util.Optional;

@Service
public class VerificationService {

    private static final Logger log = LoggerFactory.getLogger(VerificationService.class);

    private final SignatureRecordRepository signatureRecordRepository;
    private final SignatureVerificationLogRepository verificationLogRepository;
    private final HmacService hmacService;

    public VerificationService(SignatureRecordRepository signatureRecordRepository,
                               SignatureVerificationLogRepository verificationLogRepository,
                               HmacService hmacService) {
        this.signatureRecordRepository = signatureRecordRepository;
        this.verificationLogRepository = verificationLogRepository;
        this.hmacService = hmacService;
    }

    /**
     * Full verification: re-uploads the document and verifies both the document hash
     * and the HMAC-SHA256 signature value.
     */
    public VerifyResponse verifyWithDocument(MultipartFile file,
                                             String signatureCode,
                                             String verifierIp,
                                             String verifierNationalId) {
        Optional<SignatureRecord> opt = signatureRecordRepository.findBySignatureCode(signatureCode);

        if (opt.isEmpty()) {
            logVerification(signatureCode, verifierIp, verifierNationalId,
                    VerificationResult.NOT_FOUND, true);
            return VerifyResponse.builder()
                    .valid(false)
                    .signatureCode(signatureCode)
                    .documentResubmitted(true)
                    .message("Signature not found")
                    .build();
        }

        SignatureRecord record = opt.get();

        // Check revocation status first
        if (record.getStatus() == SignatureStatus.REVOKED) {
            logVerification(signatureCode, verifierIp, verifierNationalId,
                    VerificationResult.REVOKED, true);
            return VerifyResponse.builder()
                    .valid(false)
                    .signatureCode(signatureCode)
                    .signerNationalId(maskNationalId(record.getSignerNationalId()))
                    .signedAt(record.getSignedAt())
                    .status(record.getStatus())
                    .documentResubmitted(true)
                    .message("Signature has been revoked")
                    .build();
        }

        // Recompute document hash
        String uploadedHash = computeSha256(file);
        boolean hashMatch = uploadedHash.equals(record.getDocumentHash());

        // Verify HMAC
        boolean hmacValid = hmacService.verify(
                record.getDocumentHash(),
                record.getSignerNationalId(),
                record.getSignedAt(),
                record.getSignatureValue());

        boolean valid = hashMatch && hmacValid && record.getStatus() == SignatureStatus.VALID;
        VerificationResult result = valid ? VerificationResult.VALID : VerificationResult.INVALID;

        logVerification(signatureCode, verifierIp, verifierNationalId, result, true);

        log.info("Document verification: signatureCode={} hashMatch={} hmacValid={} result={}",
                signatureCode, hashMatch, hmacValid, result);

        String message = buildMessage(valid, hashMatch, hmacValid);

        return VerifyResponse.builder()
                .valid(valid)
                .signatureCode(signatureCode)
                .signerNationalId(maskNationalId(record.getSignerNationalId()))
                .signedAt(record.getSignedAt())
                .purpose(record.getPurpose())
                .documentHash(record.getDocumentHash())
                .status(record.getStatus())
                .documentResubmitted(true)
                .message(message)
                .build();
    }

    /**
     * Metadata-only verification: confirms the signature exists and is not revoked,
     * but does NOT re-verify the document hash (document not re-uploaded).
     */
    public VerifyResponse verifyByCode(String signatureCode,
                                       String verifierIp,
                                       String verifierNationalId) {
        Optional<SignatureRecord> opt = signatureRecordRepository.findBySignatureCode(signatureCode);

        if (opt.isEmpty()) {
            logVerification(signatureCode, verifierIp, verifierNationalId,
                    VerificationResult.NOT_FOUND, false);
            return VerifyResponse.builder()
                    .valid(false)
                    .signatureCode(signatureCode)
                    .documentResubmitted(false)
                    .message("Signature not found")
                    .build();
        }

        SignatureRecord record = opt.get();

        if (record.getStatus() == SignatureStatus.REVOKED) {
            logVerification(signatureCode, verifierIp, verifierNationalId,
                    VerificationResult.REVOKED, false);
            return VerifyResponse.builder()
                    .valid(false)
                    .signatureCode(signatureCode)
                    .signerNationalId(maskNationalId(record.getSignerNationalId()))
                    .signedAt(record.getSignedAt())
                    .status(record.getStatus())
                    .documentResubmitted(false)
                    .message("Signature has been revoked")
                    .build();
        }

        logVerification(signatureCode, verifierIp, verifierNationalId,
                VerificationResult.VALID, false);

        return VerifyResponse.builder()
                .valid(true)
                .signatureCode(signatureCode)
                .signerNationalId(maskNationalId(record.getSignerNationalId()))
                .signedAt(record.getSignedAt())
                .purpose(record.getPurpose())
                .documentHash(record.getDocumentHash())
                .status(record.getStatus())
                .documentResubmitted(false)
                .message("Signature record is valid. Note: document integrity not verified without re-upload.")
                .build();
    }

    // ─── Private helpers ──────────────────────────────────────────────────────

    private void logVerification(String signatureCode, String verifierIp,
                                  String verifierNationalId, VerificationResult result,
                                  boolean documentResubmitted) {
        SignatureVerificationLog log = new SignatureVerificationLog();
        log.setSignatureCode(signatureCode);
        log.setVerifierIp(verifierIp);
        log.setVerifierNationalId(verifierNationalId);
        log.setResult(result);
        log.setDocumentResubmitted(documentResubmitted);
        verificationLogRepository.save(log);
    }

    /**
     * Masks the national ID for privacy.
     * Returns first 3 chars + "****" + last 3 chars if length >= 6, else "***".
     */
    private String maskNationalId(String nationalId) {
        if (nationalId == null) return null;
        if (nationalId.length() < 6) return "***";
        return nationalId.substring(0, 3) + "****" + nationalId.substring(nationalId.length() - 3);
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
                    "Failed to compute document hash for verification");
        }
    }

    private String buildMessage(boolean valid, boolean hashMatch, boolean hmacValid) {
        if (valid) return "Signature is valid — document integrity confirmed";
        if (!hashMatch) return "Document does not match the originally signed version";
        if (!hmacValid) return "Signature value is invalid — possible tampering detected";
        return "Verification failed";
    }
}
