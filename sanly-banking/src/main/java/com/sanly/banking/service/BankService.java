package com.sanly.banking.service;

import com.sanly.banking.dto.request.RegisterBankRequest;
import com.sanly.banking.dto.response.RegisterBankResponse;
import com.sanly.banking.dto.response.RegisteredBankResponse;
import com.sanly.banking.entity.BankingOfficer;
import com.sanly.banking.entity.RegisteredBank;
import com.sanly.banking.exception.BankNotFoundException;
import com.sanly.banking.repository.RegisteredBankRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BankService {

    private static final DateTimeFormatter DT_FMT = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private final RegisteredBankRepository bankRepo;
    private final BankCodeService          bankCodeService;
    private final PasswordEncoder          passwordEncoder;

    /**
     * Registers a new bank. Generates bankCode + raw API key (shown once).
     */
    @Transactional
    public RegisterBankResponse registerBank(RegisterBankRequest request, UUID approvedByOfficerId) {
        String bankCode = bankCodeService.generateNextBankCode();
        String rawApiKey = UUID.randomUUID().toString().replace("-", ""); // 32 hex chars
        String apiKeyHash = passwordEncoder.encode(rawApiKey);

        RegisteredBank bank = RegisteredBank.builder()
                .bankCode(bankCode)
                .bankName(request.bankName())
                .licenseNumber(request.licenseNumber())
                .contactEmail(request.contactEmail())
                .apiKeyHash(apiKeyHash)
                .status("PENDING_APPROVAL")
                .registeredAt(LocalDateTime.now())
                .build();

        bankRepo.save(bank);
        log.info("Bank registered: {} ({})", bankCode, request.bankName());

        return RegisterBankResponse.builder()
                .bankCode(bankCode)
                .bankName(bank.getBankName())
                .licenseNumber(bank.getLicenseNumber())
                .contactEmail(bank.getContactEmail())
                .status(bank.getStatus())
                .registeredAt(fmt(bank.getRegisteredAt()))
                .apiKey(rawApiKey) // one-time exposure
                .build();
    }

    public List<RegisteredBankResponse> getAllBanks() {
        return bankRepo.findAll().stream().map(this::toResponse).collect(Collectors.toList());
    }

    public RegisteredBankResponse getBankByCode(String bankCode) {
        return toResponse(findOrThrow(bankCode));
    }

    /**
     * Admin approves a bank (PENDING_APPROVAL → ACTIVE).
     */
    @Transactional
    public RegisteredBankResponse approveBank(String bankCode, UUID officerId) {
        RegisteredBank bank = findOrThrow(bankCode);
        bank.setStatus("ACTIVE");
        bank.setApprovedAt(LocalDateTime.now());
        bank.setApprovedByOfficerId(officerId);
        bankRepo.save(bank);
        log.info("Bank {} approved by officer {}", bankCode, officerId);
        return toResponse(bank);
    }

    /**
     * Admin updates bank status.
     */
    @Transactional
    public RegisteredBankResponse updateBankStatus(String bankCode, String newStatus) {
        RegisteredBank bank = findOrThrow(bankCode);
        bank.setStatus(newStatus);
        bankRepo.save(bank);
        log.info("Bank {} status updated to {}", bankCode, newStatus);
        return toResponse(bank);
    }

    /**
     * Rotates API key. Returns new raw API key (one-time).
     */
    @Transactional
    public String rotateApiKey(String bankCode) {
        RegisteredBank bank = findOrThrow(bankCode);
        String newRawKey  = UUID.randomUUID().toString().replace("-", "");
        bank.setApiKeyHash(passwordEncoder.encode(newRawKey));
        bankRepo.save(bank);
        log.info("API key rotated for bank {}", bankCode);
        return newRawKey;
    }

    private RegisteredBank findOrThrow(String bankCode) {
        return bankRepo.findByBankCode(bankCode)
                .orElseThrow(() -> new BankNotFoundException("Bank not found: " + bankCode));
    }

    private RegisteredBankResponse toResponse(RegisteredBank bank) {
        return RegisteredBankResponse.builder()
                .bankCode(bank.getBankCode())
                .bankName(bank.getBankName())
                .licenseNumber(bank.getLicenseNumber())
                .contactEmail(bank.getContactEmail())
                .status(bank.getStatus())
                .registeredAt(fmt(bank.getRegisteredAt()))
                .approvedAt(fmt(bank.getApprovedAt()))
                .build();
    }

    private String fmt(LocalDateTime dt) {
        return dt != null ? dt.format(DT_FMT) : null;
    }
}
