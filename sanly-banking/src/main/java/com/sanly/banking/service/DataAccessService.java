package com.sanly.banking.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sanly.banking.client.*;
import com.sanly.banking.dto.response.CitizenProfileResponse;
import com.sanly.banking.entity.BankDataAccess;
import com.sanly.banking.entity.ConsentScope;
import com.sanly.banking.entity.ConsentToken;
import com.sanly.banking.repository.BankDataAccessRepository;
import com.sanly.banking.repository.RegisteredBankRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DataAccessService {

    private static final DateTimeFormatter DT_FMT = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private final BankAuthService            bankAuthService;
    private final TokenService               tokenService;
    private final BankDataAccessRepository   accessRepo;
    private final RegisteredBankRepository   bankRepo;
    private final CitizenRegistryClient      citizenClient;
    private final TaxDataClient              taxClient;
    private final CriminalClearanceClient    criminalClient;
    private final BusinessOwnershipClient    businessClient;
    private final PropertyOwnershipClient    propertyClient;
    private final PensionStatusClient        pensionClient;
    private final DrivingLicenseClient       drivingClient;
    private final NotificationClient         notificationClient;
    private final ObjectMapper               objectMapper;

    /**
     * Fetches citizen profile data based on consent token scopes.
     * Token is consumed BEFORE data is fetched (critical: single-use enforcement).
     */
    @Transactional
    public CitizenProfileResponse fetchCitizenProfile(String bankCode, String bankKey,
                                                       String consentTokenPlain,
                                                       String ipAddress) {

        // 1. Validate bank
        bankAuthService.validateBank(bankCode, bankKey);

        // 2. Validate and consume token (SINGLE-USE: consumed before data fetch)
        ConsentToken token = tokenService.validateAndConsume(consentTokenPlain, bankCode);

        String citizenNationalId = token.getCitizenNationalId();
        String consentCode       = token.getConsentCode();
        List<ConsentScope> grantedScopes = parseScopes(token.getGrantedScopes());

        Map<String, Object> data = new LinkedHashMap<>();
        String responseStatus = "SUCCESS";
        List<String> accessedScopeNames = new ArrayList<>();
        boolean anyFailed = false;

        // 3. Fetch data per scope
        for (ConsentScope scope : grantedScopes) {
            accessedScopeNames.add(scope.name());
            try {
                switch (scope) {
                    case IDENTITY_BASIC -> {
                        CitizenRegistryClient.CitizenInfo info = citizenClient.getCitizenBasic(citizenNationalId);
                        if (info != null) {
                            data.put("identity_basic", Map.of(
                                    "nationalId", maskNationalId(info.getNationalId()),
                                    "firstName", info.getFirstName(),
                                    "lastName",  info.getLastName(),
                                    "dateOfBirth", info.getDateOfBirth()
                            ));
                        } else {
                            data.put("identity_basic", "UNAVAILABLE");
                            anyFailed = true;
                        }
                    }
                    case IDENTITY_FULL -> {
                        CitizenRegistryClient.CitizenInfo info = citizenClient.getCitizenBasic(citizenNationalId);
                        if (info != null) {
                            Map<String, Object> identity = new LinkedHashMap<>();
                            identity.put("nationalId",   maskNationalId(info.getNationalId()));
                            identity.put("firstName",    info.getFirstName());
                            identity.put("lastName",     info.getLastName());
                            identity.put("dateOfBirth",  info.getDateOfBirth());
                            identity.put("gender",       info.getGender());
                            if (info.getAddress()     != null) identity.put("address", info.getAddress());
                            if (info.getMaskedPhone()  != null) identity.put("maskedPhone", info.getMaskedPhone());
                            data.put("identity_full", identity);
                        } else {
                            data.put("identity_full", "UNAVAILABLE");
                            anyFailed = true;
                        }
                    }
                    case TAX_STATUS -> {
                        TaxDataClient.TaxInfo tax = taxClient.getTaxInfo(citizenNationalId);
                        if (tax != null) {
                            data.put("tax_status", Map.of(
                                    "complianceStatus", tax.getComplianceStatus(),
                                    "lastFilingYear",   tax.getLastFilingYear() != null ? tax.getLastFilingYear() : "N/A"
                            ));
                        } else {
                            data.put("tax_status", "UNAVAILABLE");
                            anyFailed = true;
                        }
                    }
                    case TAX_INCOME_CLASS -> {
                        TaxDataClient.TaxInfo tax = taxClient.getTaxInfo(citizenNationalId);
                        if (tax != null) {
                            String incomeClass = tax.getIncomeClass() != null ? tax.getIncomeClass() : "UNKNOWN";
                            data.put("tax_income_class", Map.of("incomeClass", incomeClass));
                        } else {
                            data.put("tax_income_class", "UNAVAILABLE");
                            anyFailed = true;
                        }
                    }
                    case CRIMINAL_CLEARANCE -> {
                        CriminalClearanceClient.ClearanceInfo clearance = criminalClient.getClearance(citizenNationalId);
                        if (clearance != null) {
                            // Binary only: CLEAR or HAS_RECORD — never details
                            data.put("criminal_clearance", Map.of("status", clearance.getStatus()));
                        } else {
                            data.put("criminal_clearance", "UNAVAILABLE");
                            anyFailed = true;
                        }
                    }
                    case BUSINESS_OWNERSHIP -> {
                        List<BusinessOwnershipClient.BusinessInfo> businesses = businessClient.getOwnedBusinesses(citizenNationalId);
                        if (businesses != null) {
                            List<Map<String, String>> bizList = businesses.stream()
                                    .map(b -> Map.of(
                                            "name", b.getName(),
                                            "registrationNumber", b.getRegistrationNumber(),
                                            "status", b.getStatus()
                                    ))
                                    .collect(Collectors.toList());
                            data.put("business_ownership", Map.of("businesses", bizList, "count", bizList.size()));
                        } else {
                            data.put("business_ownership", "UNAVAILABLE");
                            anyFailed = true;
                        }
                    }
                    case PROPERTY_OWNERSHIP -> {
                        PropertyOwnershipClient.PropertyInfo prop = propertyClient.getPropertyInfo(citizenNationalId);
                        if (prop != null) {
                            Map<String, Object> propMap = new LinkedHashMap<>();
                            propMap.put("count", prop.getCount());
                            if (prop.getTotalValueRange() != null) {
                                propMap.put("totalValueRange", prop.getTotalValueRange());
                            }
                            data.put("property_ownership", propMap);
                        } else {
                            data.put("property_ownership", "UNAVAILABLE");
                            anyFailed = true;
                        }
                    }
                    case PENSION_STATUS -> {
                        PensionStatusClient.PensionInfo pension = pensionClient.getPensionInfo(citizenNationalId);
                        if (pension != null) {
                            data.put("pension_status", Map.of(
                                    "accountStatus",    pension.getAccountStatus(),
                                    "estimatedMonthly", pension.getEstimatedMonthly()
                            ));
                        } else {
                            data.put("pension_status", "UNAVAILABLE");
                            anyFailed = true;
                        }
                    }
                    case EMPLOYMENT_STATUS -> {
                        PensionStatusClient.PensionInfo pension = pensionClient.getPensionInfo(citizenNationalId);
                        if (pension != null) {
                            data.put("employment_status", Map.of(
                                    "activeContributor", pension.isActiveContributor()
                            ));
                        } else {
                            data.put("employment_status", "UNAVAILABLE");
                            anyFailed = true;
                        }
                    }
                    case MEDICAL_CLEARANCE -> {
                        // Medical clearance: binary yes/no — placeholder for medical service
                        data.put("medical_clearance", Map.of("status", "UNAVAILABLE"));
                        anyFailed = true;
                    }
                    case DRIVING_LICENSE -> {
                        DrivingLicenseClient.LicenseInfo license = drivingClient.getLicenseInfo(citizenNationalId);
                        if (license != null) {
                            Map<String, Object> licMap = new LinkedHashMap<>();
                            licMap.put("status", license.getStatus());
                            licMap.put("categories", license.getCategories() != null ? license.getCategories() : List.of());
                            if (license.getExpiryDate() != null) licMap.put("expiryDate", license.getExpiryDate());
                            data.put("driving_license", licMap);
                        } else {
                            data.put("driving_license", "UNAVAILABLE");
                            anyFailed = true;
                        }
                    }
                }
            } catch (Exception e) {
                log.warn("Failed to fetch scope {} for citizen {}: {}", scope, citizenNationalId, e.getMessage());
                data.put(scope.name().toLowerCase(), "UNAVAILABLE");
                anyFailed = true;
            }
        }

        if (anyFailed && !data.values().stream().allMatch(v -> "UNAVAILABLE".equals(v))) {
            responseStatus = "PARTIAL";
        } else if (data.values().stream().allMatch(v -> "UNAVAILABLE".equals(v))) {
            responseStatus = "FAILED";
        }

        // 4. Save access log
        String scopesJson = "[" + accessedScopeNames.stream()
                .map(s -> "\"" + s + "\"").collect(Collectors.joining(",")) + "]";

        BankDataAccess accessLog = BankDataAccess.builder()
                .bankCode(bankCode)
                .citizenNationalId(citizenNationalId)
                .consentCode(consentCode)
                .scopesAccessed(scopesJson)
                .accessedAt(LocalDateTime.now())
                .ipAddress(ipAddress)
                .responseStatus(responseStatus)
                .build();
        accessRepo.save(accessLog);

        // 5. Notify citizen of data access (async)
        notificationClient.send(citizenNationalId, "BANKING_DATA_ACCESSED", "EN",
                Map.of("bankCode", bankCode, "consentCode", consentCode));

        return CitizenProfileResponse.builder()
                .consentCode(consentCode)
                .bankCode(bankCode)
                .accessedAt(LocalDateTime.now().format(DT_FMT))
                .tokenExpiresAt(token.getExpiresAt().format(DT_FMT))
                .scopesAccessed(accessedScopeNames)
                .data(data)
                .build();
    }

    // ===================== Access history =====================

    public Page<BankDataAccess> getAccessHistoryForCitizen(String citizenNationalId, Pageable pageable) {
        return accessRepo.findByCitizenNationalIdOrderByAccessedAtDesc(citizenNationalId, pageable);
    }

    public Page<BankDataAccess> getAccessHistoryForBank(String bankCode, Pageable pageable) {
        return accessRepo.findByBankCodeOrderByAccessedAtDesc(bankCode, pageable);
    }

    public Page<BankDataAccess> getAllAccessHistory(Pageable pageable) {
        return accessRepo.findAllByOrderByAccessedAtDesc(pageable);
    }

    // ===================== Helpers =====================

    private List<ConsentScope> parseScopes(String grantedScopesJson) {
        List<ConsentScope> scopes = new ArrayList<>();
        if (grantedScopesJson == null) return scopes;
        try {
            List<String> names = objectMapper.readValue(grantedScopesJson, new TypeReference<List<String>>() {});
            for (String name : names) {
                try { scopes.add(ConsentScope.valueOf(name)); }
                catch (IllegalArgumentException ignored) {}
            }
        } catch (Exception e) {
            log.warn("Failed to parse grantedScopes JSON: {}", grantedScopesJson);
        }
        return scopes;
    }

    private String maskNationalId(String nationalId) {
        if (nationalId == null || nationalId.length() < 4) return "****";
        return nationalId.substring(0, 4) + "*".repeat(Math.max(0, nationalId.length() - 4));
    }
}
