package com.sanly.documents.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sanly.documents.client.NotificationClient;
import com.sanly.documents.dto.response.CertificateResponse;
import com.sanly.documents.dto.response.DocStatsResponse;
import com.sanly.documents.dto.response.PageResponse;
import com.sanly.documents.dto.response.VerifyResponse;
import com.sanly.documents.entity.*;
import com.sanly.documents.repository.CertificateVerificationLogRepository;
import com.sanly.documents.repository.DocumentCertificateRepository;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CertificateService {

    private final DocumentCertificateRepository certRepo;
    private final CertificateVerificationLogRepository verifyLogRepo;
    private final CertificateCodeService codeService;
    private final HashService hashService;
    private final PdfGenerationService pdfService;
    private final SourceDataService sourceDataService;
    private final NotificationClient notificationClient;
    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate;

    @Value("${registry.base-url:http://localhost:8080}")
    private String registryUrl;

    @Value("${registry.institution-token}")
    private String institutionToken;

    @Value("${rate-limit.cert-capacity:20}")
    private int certCapacity;

    @Value("${rate-limit.cert-refill-hours:1}")
    private int certRefillHours;

    // Per-citizen rate limiting for certificate generation
    private final Map<String, Bucket> citizenBuckets = new ConcurrentHashMap<>();

    private Bucket citizenBucket(String nationalId) {
        return citizenBuckets.computeIfAbsent(nationalId, k ->
                Bucket.builder()
                        .addLimit(Bandwidth.classic(
                                certCapacity,
                                Refill.greedy(certCapacity, Duration.ofHours(certRefillHours))))
                        .build());
    }

    /**
     * Generates a certified digital PDF for the requested document type.
     * Rate-limited to 20 per hour per citizen.
     */
    @Transactional
    public byte[] generate(DocumentType docType, String sourceRecordCode,
                           String citizenNationalId, String citizenToken) {
        if (!citizenBucket(citizenNationalId).tryConsume(1)) {
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS,
                    "Certificate generation rate limit exceeded. Maximum " + certCapacity
                    + " certificates per " + certRefillHours + " hour(s).");
        }

        String certCode  = codeService.generateCode();
        LocalDateTime issuedAt = LocalDateTime.now();
        String hash = hashService.computeHash(
                certCode, citizenNationalId, docType.name(),
                sourceRecordCode != null ? sourceRecordCode : "",
                issuedAt.toString()
        );

        // Fetch source data from the appropriate service
        Map<String, String> fieldData = sourceDataService.fetchDocumentData(docType, sourceRecordCode, citizenToken);

        // Fetch citizen name from registry
        String holderName = fetchCitizenName(citizenNationalId, citizenToken);

        // Build certificate title
        String title = Arrays.stream(docType.name().split("_"))
                .map(word -> word.substring(0, 1).toUpperCase() + word.substring(1).toLowerCase())
                .collect(Collectors.joining(" "));

        // Determine source service from docType
        SourceService sourceService = resolveSourceService(docType);

        // Serialize fieldData as JSON for storage (for later regeneration)
        String pdfContentJson = null;
        try {
            pdfContentJson = objectMapper.writeValueAsString(fieldData);
        } catch (Exception e) {
            log.warn("Failed to serialize fieldData for cert {}", certCode);
        }

        DocumentCertificate cert = new DocumentCertificate();
        cert.setCertificateCode(certCode);
        cert.setCitizenNationalId(citizenNationalId);
        cert.setHolderName(holderName);
        cert.setDocumentType(docType);
        cert.setSourceService(sourceService);
        cert.setSourceRecordCode(sourceRecordCode);
        cert.setTitle(title);
        cert.setIssuedAt(issuedAt);
        cert.setStatus(CertificateStatus.VALID);
        cert.setVerificationHash(hash);
        cert.setDownloadCount(0);
        cert.setPdfContent(pdfContentJson);

        cert = certRepo.save(cert);

        byte[] pdfBytes = pdfService.generateCertificatePdf(cert, fieldData);

        // Async notification
        final String finalCertCode = certCode;
        final String finalNationalId = citizenNationalId;
        try {
            notificationClient.send(finalNationalId, "CERTIFICATE_GENERATED", "tk",
                    Map.of("certCode", finalCertCode, "docType", docType.name(), "title", title));
        } catch (Exception e) {
            log.warn("Notification failed for CERTIFICATE_GENERATED: {}", e.getMessage());
        }

        return pdfBytes;
    }

    /**
     * Downloads (re-generates) the PDF for a certificate.
     */
    @Transactional
    public byte[] download(String certCode, String nationalId, boolean isAdmin) {
        DocumentCertificate cert = findCert(certCode);

        if (!isAdmin && !cert.getCitizenNationalId().equals(nationalId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied to this certificate");
        }
        if (cert.getStatus() == CertificateStatus.REVOKED) {
            throw new ResponseStatusException(HttpStatus.GONE, "This certificate has been revoked");
        }

        Map<String, String> fieldData = deserializeFieldData(cert.getPdfContent());

        cert.setDownloadCount(cert.getDownloadCount() + 1);
        cert.setLastDownloadedAt(LocalDateTime.now());
        certRepo.save(cert);

        return pdfService.generateCertificatePdf(cert, fieldData);
    }

    /**
     * Revokes a certificate.
     */
    @Transactional
    public void revoke(String certCode, String nationalId, String reason, boolean isAdmin) {
        DocumentCertificate cert = findCert(certCode);

        if (!isAdmin && !cert.getCitizenNationalId().equals(nationalId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied to this certificate");
        }
        if (cert.getStatus() == CertificateStatus.REVOKED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Certificate is already revoked");
        }

        cert.setStatus(CertificateStatus.REVOKED);
        cert.setRevokedAt(LocalDateTime.now());
        cert.setRevokeReason(reason);
        certRepo.save(cert);
    }

    /**
     * Returns all certificates for a citizen.
     */
    @Transactional(readOnly = true)
    public List<CertificateResponse> getMyList(String nationalId) {
        return certRepo.findByCitizenNationalIdOrderByIssuedAtDesc(nationalId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Returns metadata for a specific certificate, checking ownership.
     */
    @Transactional(readOnly = true)
    public CertificateResponse getByCode(String certCode, String nationalId, boolean isAdmin) {
        DocumentCertificate cert = findCert(certCode);
        if (!isAdmin && !cert.getCitizenNationalId().equals(nationalId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied to this certificate");
        }
        return toResponse(cert);
    }

    /**
     * Verifies a certificate by code. Logs the verification attempt.
     * Notifies the owner if the cert was verified by a third party.
     */
    @Transactional
    public VerifyResponse verifyByCode(String certCode, String verifierIp, String verifierNationalId) {
        Optional<DocumentCertificate> certOpt = certRepo.findByCertificateCode(certCode);

        if (certOpt.isEmpty()) {
            logVerification(certCode, verifierIp, verifierNationalId, VerificationResult.INVALID);
            return VerifyResponse.builder()
                    .valid(false)
                    .certificateCode(certCode)
                    .message("Certificate not found")
                    .build();
        }

        DocumentCertificate cert = certOpt.get();

        // Check hash integrity
        if (!hashService.verify(cert)) {
            logVerification(certCode, verifierIp, verifierNationalId, VerificationResult.INVALID);
            return VerifyResponse.builder()
                    .valid(false)
                    .certificateCode(certCode)
                    .message("Certificate integrity check failed — document may be tampered")
                    .build();
        }

        // Check revoked
        if (cert.getStatus() == CertificateStatus.REVOKED) {
            logVerification(certCode, verifierIp, verifierNationalId, VerificationResult.REVOKED);
            return VerifyResponse.builder()
                    .valid(false)
                    .certificateCode(certCode)
                    .documentType(cert.getDocumentType())
                    .holderName(cert.getHolderName())
                    .holderNationalId(maskNationalId(cert.getCitizenNationalId()))
                    .issuedAt(cert.getIssuedAt())
                    .status(cert.getStatus())
                    .sourceService(cert.getSourceService())
                    .message("Certificate has been revoked")
                    .build();
        }

        // Check expired
        if (cert.getExpiresAt() != null && cert.getExpiresAt().isBefore(LocalDateTime.now())) {
            logVerification(certCode, verifierIp, verifierNationalId, VerificationResult.EXPIRED);
            return VerifyResponse.builder()
                    .valid(false)
                    .certificateCode(certCode)
                    .documentType(cert.getDocumentType())
                    .holderName(cert.getHolderName())
                    .holderNationalId(maskNationalId(cert.getCitizenNationalId()))
                    .issuedAt(cert.getIssuedAt())
                    .expiresAt(cert.getExpiresAt())
                    .status(CertificateStatus.EXPIRED)
                    .sourceService(cert.getSourceService())
                    .message("Certificate has expired")
                    .build();
        }

        // Valid
        logVerification(certCode, verifierIp, verifierNationalId, VerificationResult.VALID);

        // Async: notify owner if verified by someone else (verifierNationalId != owner)
        if (verifierNationalId != null
                && !verifierNationalId.equals(cert.getCitizenNationalId())) {
            try {
                notificationClient.send(cert.getCitizenNationalId(),
                        "CERTIFICATE_VERIFIED_BY_THIRD_PARTY", "tk",
                        Map.of(
                                "certCode", certCode,
                                "verifierIp", verifierIp != null ? verifierIp : "unknown",
                                "docType", cert.getDocumentType().name()
                        ));
            } catch (Exception e) {
                log.warn("Notification failed for CERTIFICATE_VERIFIED_BY_THIRD_PARTY: {}", e.getMessage());
            }
        }

        return VerifyResponse.builder()
                .valid(true)
                .certificateCode(certCode)
                .documentType(cert.getDocumentType())
                .holderName(cert.getHolderName())
                .holderNationalId(maskNationalId(cert.getCitizenNationalId()))
                .issuedAt(cert.getIssuedAt())
                .expiresAt(cert.getExpiresAt())
                .status(cert.getStatus())
                .sourceService(cert.getSourceService())
                .sourceRecordCode(cert.getSourceRecordCode())
                .message("Certificate is valid")
                .build();
    }

    /**
     * Admin: paginated list of all certificates.
     */
    @Transactional(readOnly = true)
    public PageResponse<CertificateResponse> adminGetAll(int page, int size) {
        Page<DocumentCertificate> pg = certRepo.findAllByOrderByIssuedAtDesc(PageRequest.of(page, size));
        return PageResponse.<CertificateResponse>builder()
                .content(pg.getContent().stream().map(this::toResponse).collect(Collectors.toList()))
                .page(pg.getNumber())
                .size(pg.getSize())
                .totalElements(pg.getTotalElements())
                .totalPages(pg.getTotalPages())
                .last(pg.isLast())
                .build();
    }

    /**
     * Admin: revoke a certificate and return updated response.
     */
    @Transactional
    public CertificateResponse adminRevoke(String certCode, String reason) {
        DocumentCertificate cert = findCert(certCode);
        cert.setStatus(CertificateStatus.REVOKED);
        cert.setRevokedAt(LocalDateTime.now());
        cert.setRevokeReason(reason);
        cert = certRepo.save(cert);
        return toResponse(cert);
    }

    /**
     * Returns service-wide statistics for the admin dashboard.
     */
    @Transactional(readOnly = true)
    public DocStatsResponse getStats() {
        LocalDateTime startOfDay = LocalDateTime.now().toLocalDate().atStartOfDay();

        long certsToday = certRepo.findAllByOrderByIssuedAtDesc(PageRequest.of(0, Integer.MAX_VALUE))
                .getContent().stream()
                .filter(c -> c.getIssuedAt().isAfter(startOfDay))
                .count();

        long verificationsToday = verifyLogRepo.countByVerifiedAtAfter(startOfDay);
        long activeCerts = certRepo.countByStatus(CertificateStatus.VALID);

        // Find the most common document type
        Map<DocumentType, Long> typeCounts = new HashMap<>();
        for (DocumentType dt : DocumentType.values()) {
            long count = certRepo.countByDocumentTypeAndIssuedAtAfter(dt,
                    LocalDateTime.now().minusYears(100));
            if (count > 0) typeCounts.put(dt, count);
        }
        String topType = typeCounts.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(e -> e.getKey().name())
                .orElse("N/A");

        return DocStatsResponse.builder()
                .certsGeneratedToday(certsToday)
                .verificationsToday(verificationsToday)
                .activeCerts(activeCerts)
                .topDocumentType(topType)
                .build();
    }

    // ---- Private helpers ----

    private DocumentCertificate findCert(String certCode) {
        return certRepo.findByCertificateCode(certCode)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Certificate not found: " + certCode));
    }

    private void logVerification(String certCode, String ip, String nationalId, VerificationResult result) {
        CertificateVerificationLog log = new CertificateVerificationLog();
        log.setCertificateCode(certCode);
        log.setVerifierIp(ip);
        log.setVerifierNationalId(nationalId);
        log.setResult(result);
        verifyLogRepo.save(log);
    }

    private CertificateResponse toResponse(DocumentCertificate c) {
        long verifyCount = verifyLogRepo.findByCertificateCodeOrderByVerifiedAtDesc(c.getCertificateCode()).size();
        return CertificateResponse.builder()
                .certificateId(c.getCertificateId())
                .certificateCode(c.getCertificateCode())
                .citizenNationalId(c.getCitizenNationalId())
                .holderName(c.getHolderName())
                .documentType(c.getDocumentType())
                .sourceService(c.getSourceService())
                .sourceRecordCode(c.getSourceRecordCode())
                .title(c.getTitle())
                .issuedAt(c.getIssuedAt())
                .expiresAt(c.getExpiresAt())
                .status(c.getStatus())
                .verificationHash(c.getVerificationHash())
                .downloadCount(c.getDownloadCount())
                .lastDownloadedAt(c.getLastDownloadedAt())
                .revokedAt(c.getRevokedAt())
                .revokeReason(c.getRevokeReason())
                .verificationCount(verifyCount)
                .build();
    }

    @SuppressWarnings("unchecked")
    private Map<String, String> deserializeFieldData(String json) {
        if (json == null || json.isBlank()) return new HashMap<>();
        try {
            return objectMapper.readValue(json, new TypeReference<Map<String, String>>() {});
        } catch (Exception e) {
            log.warn("Failed to deserialize pdfContent: {}", e.getMessage());
            return new HashMap<>();
        }
    }

    private String fetchCitizenName(String nationalId, String citizenToken) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(institutionToken);
            HttpEntity<Void> entity = new HttpEntity<>(headers);
            ResponseEntity<Map> resp = restTemplate.exchange(
                    registryUrl + "/api/v1/citizens/" + nationalId + "/verify",
                    HttpMethod.GET, entity, Map.class);
            if (resp.getBody() != null) {
                Object data = resp.getBody().get("data");
                if (data instanceof Map<?, ?> dataMap) {
                    Object fullName = dataMap.get("fullName");
                    if (fullName instanceof String name && !name.isBlank()) {
                        return name;
                    }
                    Object firstName = dataMap.get("firstName");
                    Object lastName  = dataMap.get("lastName");
                    if (firstName != null && lastName != null) {
                        return firstName + " " + lastName;
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Could not fetch citizen name for NIN={}: {}", nationalId, e.getMessage());
        }
        return nationalId; // fallback
    }

    private String maskNationalId(String nationalId) {
        if (nationalId == null || nationalId.length() < 4) return "***";
        return "***" + nationalId.substring(nationalId.length() - 4) + "***";
    }

    private SourceService resolveSourceService(DocumentType docType) {
        return switch (docType) {
            case BIRTH_CERTIFICATE, MARRIAGE_CERTIFICATE, DEATH_CERTIFICATE, NATIONAL_ID -> SourceService.CIVIL;
            case DRIVING_LICENSE -> SourceService.DMV;
            case DIPLOMA -> SourceService.EDUCATION;
            case PROPERTY_DEED -> SourceService.LAND;
            case BUSINESS_REGISTRATION -> SourceService.BUSINESS;
            case TAX_CLEARANCE -> SourceService.TAX;
            case CRIMINAL_CLEARANCE -> SourceService.POLICE;
            case MEDICAL_CLEARANCE -> SourceService.MEDICAL;
            case CUSTOMS_CLEARANCE -> SourceService.CUSTOMS;
            case COURT_ORDER -> SourceService.COURT;
            case PENSION_CERTIFICATE -> SourceService.PENSION;
            case VEHICLE_REGISTRATION -> SourceService.VEHICLE;
            case SOCIAL_BENEFIT_LETTER -> SourceService.CIVIL;
        };
    }
}
