package com.sanly.banking.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sanly.banking.client.CitizenRegistryClient;
import com.sanly.banking.client.NotificationClient;
import com.sanly.banking.client.RegistryClient;
import com.sanly.banking.config.EncryptionService;
import com.sanly.banking.dto.response.ConsentRequestResponse;
import com.sanly.banking.dto.response.ConsentStatusResponse;
import com.sanly.banking.dto.response.ScopeDescription;
import com.sanly.banking.entity.ConsentRequest;
import com.sanly.banking.entity.ConsentScope;
import com.sanly.banking.entity.RegisteredBank;
import com.sanly.banking.exception.CitizenNotFoundException;
import com.sanly.banking.exception.ConsentExpiredException;
import com.sanly.banking.exception.ConsentNotFoundException;
import com.sanly.banking.exception.OtpVerificationException;
import com.sanly.banking.repository.ConsentRequestRepository;
import com.sanly.banking.repository.RegisteredBankRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConsentService {

    private static final DateTimeFormatter DT_FMT = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private final ConsentRequestRepository consentRepo;
    private final RegisteredBankRepository bankRepo;
    private final BankAuthService          bankAuthService;
    private final ConsentCodeService       consentCodeService;
    private final TokenService             tokenService;
    private final EncryptionService        encryptionService;
    private final ScopeDescriptionMapper   scopeDescMapper;
    private final CitizenRegistryClient    citizenClient;
    private final RegistryClient           registryClient;
    private final NotificationClient       notificationClient;
    private final ObjectMapper             objectMapper;

    // ===================== Bank-facing =====================

    /**
     * Bank requests consent from a citizen.
     */
    @Transactional
    public ConsentRequest requestConsent(String bankCode, String bankKey,
                                         String citizenNationalId,
                                         List<String> requestedScopes,
                                         String purpose) {

        RegisteredBank bank = bankAuthService.validateBank(bankCode, bankKey);

        // Verify citizen exists
        if (!citizenClient.citizenExists(citizenNationalId)) {
            throw new CitizenNotFoundException("Citizen not found: " + citizenNationalId);
        }

        // Generate consent code
        String consentCode = consentCodeService.generateNextConsentCode();

        // Encrypt purpose
        String encryptedPurpose = encryptionService.encrypt(purpose);

        // Serialize scopes
        String scopesJson = scopesToJson(requestedScopes);

        ConsentRequest consent = ConsentRequest.builder()
                .consentCode(consentCode)
                .bankCode(bankCode)
                .citizenNationalId(citizenNationalId)
                .requestedScopes(scopesJson)
                .purpose(encryptedPurpose)
                .status("PENDING")
                .requestedAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusMinutes(10))
                .otpVerified(false)
                .build();

        consentRepo.save(consent);

        // Notify citizen via SMS (async)
        notificationClient.send(citizenNationalId, "BANKING_CONSENT_REQUESTED", "EN",
                Map.of("bankName", bank.getBankName(),
                       "consentCode", consentCode,
                       "expiresIn", "10 minutes"));

        log.info("Consent request {} created for citizen {} by bank {}", consentCode, citizenNationalId, bankCode);
        return consent;
    }

    /**
     * Bank polls consent status. If APPROVED, returns plain token once and clears cache.
     */
    @Transactional
    public ConsentStatusResponse getConsentStatus(String consentCode) {
        ConsentRequest consent = consentRepo.findByConsentCode(consentCode)
                .orElseThrow(() -> new ConsentNotFoundException("Consent not found: " + consentCode));

        String token = null;
        if ("APPROVED".equals(consent.getStatus()) && consent.getPlainTokenCache() != null) {
            token = consent.getPlainTokenCache();
            consent.setPlainTokenCache(null); // clear after retrieval
            consentRepo.save(consent);
            log.info("Plain token delivered to bank for consent {} — cache cleared", consentCode);
        }

        return ConsentStatusResponse.builder()
                .consentCode(consentCode)
                .status(consent.getStatus())
                .consentToken(token)
                .build();
    }

    // ===================== Citizen-facing =====================

    /**
     * Returns pending consent requests for a citizen (citizen's dashboard).
     */
    public List<ConsentRequestResponse> getPendingForCitizen(String citizenNationalId) {
        List<ConsentRequest> pending = consentRepo.findByCitizenNationalIdAndStatus(
                citizenNationalId, "PENDING");
        return pending.stream()
                .filter(c -> LocalDateTime.now().isBefore(c.getExpiresAt()))
                .map(c -> toResponse(c, true))
                .collect(Collectors.toList());
    }

    /**
     * Returns full consent details for a citizen.
     */
    public ConsentRequestResponse getConsentDetails(String consentCode, String citizenNationalId) {
        ConsentRequest consent = consentRepo.findByConsentCode(consentCode)
                .orElseThrow(() -> new ConsentNotFoundException("Consent not found: " + consentCode));

        if (!citizenNationalId.equals(consent.getCitizenNationalId())) {
            throw new ConsentNotFoundException("Consent not found for this citizen");
        }

        return toResponse(consent, true);
    }

    /**
     * Citizen approves consent with OTP. Returns plain token.
     */
    @Transactional
    public String approveConsent(String consentCode, String citizenNationalId,
                                  List<String> approvedScopes, String otpCode) {

        ConsentRequest consent = consentRepo.findByConsentCode(consentCode)
                .orElseThrow(() -> new ConsentNotFoundException("Consent not found: " + consentCode));

        if (!citizenNationalId.equals(consent.getCitizenNationalId())) {
            throw new ConsentNotFoundException("Consent not found for this citizen");
        }
        if (!"PENDING".equals(consent.getStatus())) {
            throw new ConsentExpiredException("Consent is not in PENDING state (current: " + consent.getStatus() + ")");
        }
        if (LocalDateTime.now().isAfter(consent.getExpiresAt())) {
            consent.setStatus("EXPIRED");
            consentRepo.save(consent);
            throw new ConsentExpiredException("Consent request has expired");
        }

        // Verify OTP via registry
        Boolean otpResult = registryClient.verifyActionOtp(citizenNationalId, otpCode);
        if (otpResult == null) {
            throw new OtpVerificationException("OTP verification service is unavailable. Please try again later.");
        }
        if (!otpResult) {
            throw new OtpVerificationException("Invalid OTP code");
        }

        // Parse approved scopes
        List<ConsentScope> grantedScopeEnums = approvedScopes.stream()
                .map(s -> {
                    try { return ConsentScope.valueOf(s); }
                    catch (IllegalArgumentException e) { return null; }
                })
                .filter(s -> s != null)
                .collect(Collectors.toList());

        // Generate consent token
        String plainToken = tokenService.generateToken(consent, grantedScopeEnums);

        // Update consent
        consent.setStatus("APPROVED");
        consent.setApprovedAt(LocalDateTime.now());
        consent.setOtpVerified(true);
        consent.setPlainTokenCache(plainToken); // bank retrieves via polling
        consentRepo.save(consent);

        // Notify citizen
        RegisteredBank bank = bankRepo.findByBankCode(consent.getBankCode()).orElse(null);
        String bankName = bank != null ? bank.getBankName() : consent.getBankCode();
        notificationClient.send(citizenNationalId, "BANKING_CONSENT_APPROVED", "EN",
                Map.of("bankName", bankName, "consentCode", consentCode));

        log.info("Consent {} approved by citizen {}", consentCode, citizenNationalId);
        return plainToken;
    }

    /**
     * Citizen rejects consent.
     */
    @Transactional
    public void rejectConsent(String consentCode, String citizenNationalId) {
        ConsentRequest consent = consentRepo.findByConsentCode(consentCode)
                .orElseThrow(() -> new ConsentNotFoundException("Consent not found: " + consentCode));

        if (!citizenNationalId.equals(consent.getCitizenNationalId())) {
            throw new ConsentNotFoundException("Consent not found for this citizen");
        }
        if (!"PENDING".equals(consent.getStatus())) {
            throw new ConsentExpiredException("Consent is not in PENDING state");
        }

        consent.setStatus("REJECTED");
        consent.setRejectedAt(LocalDateTime.now());
        consentRepo.save(consent);

        RegisteredBank bank = bankRepo.findByBankCode(consent.getBankCode()).orElse(null);
        String bankName = bank != null ? bank.getBankName() : consent.getBankCode();
        notificationClient.send(citizenNationalId, "BANKING_CONSENT_REJECTED", "EN",
                Map.of("bankName", bankName, "consentCode", consentCode));

        log.info("Consent {} rejected by citizen {}", consentCode, citizenNationalId);
    }

    // ===================== Admin =====================

    public Page<ConsentRequest> getAllConsents(Pageable pageable) {
        return consentRepo.findAllByOrderByRequestedAtDesc(pageable);
    }

    // ===================== Helpers =====================

    private ConsentRequestResponse toResponse(ConsentRequest consent, boolean decryptPurpose) {
        RegisteredBank bank = bankRepo.findByBankCode(consent.getBankCode()).orElse(null);
        String bankName = bank != null ? bank.getBankName() : consent.getBankCode();

        String purpose = consent.getPurpose();
        if (decryptPurpose && purpose != null) {
            try { purpose = encryptionService.decrypt(purpose); }
            catch (Exception e) { purpose = "[decryption error]"; }
        }

        List<ScopeDescription> scopeDescriptions = parseScopeDescriptions(consent.getRequestedScopes());

        return ConsentRequestResponse.builder()
                .consentCode(consent.getConsentCode())
                .bankCode(consent.getBankCode())
                .bankName(bankName)
                .status(consent.getStatus())
                .requestedAt(consent.getRequestedAt() != null ? consent.getRequestedAt().format(DT_FMT) : null)
                .expiresAt(consent.getExpiresAt() != null ? consent.getExpiresAt().format(DT_FMT) : null)
                .requestedScopes(scopeDescriptions)
                .purpose(purpose)
                .build();
    }

    private List<ScopeDescription> parseScopeDescriptions(String scopesJson) {
        List<ScopeDescription> result = new ArrayList<>();
        if (scopesJson == null) return result;
        try {
            List<String> scopeNames = objectMapper.readValue(scopesJson, new TypeReference<List<String>>() {});
            for (String name : scopeNames) {
                try {
                    ConsentScope scope = ConsentScope.valueOf(name);
                    result.add(scopeDescMapper.getDescription(scope));
                } catch (IllegalArgumentException ignored) {}
            }
        } catch (Exception e) {
            log.warn("Failed to parse scopes JSON: {}", scopesJson);
        }
        return result;
    }

    private String scopesToJson(List<String> scopes) {
        return "[" + scopes.stream().map(s -> "\"" + s + "\"").collect(Collectors.joining(",")) + "]";
    }
}
