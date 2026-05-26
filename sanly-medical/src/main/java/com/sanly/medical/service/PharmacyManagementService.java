package com.sanly.medical.service;

import com.sanly.medical.dto.request.PharmacyStatusRequest;
import com.sanly.medical.dto.request.RegisterPharmacyRequest;
import com.sanly.medical.dto.response.PharmacyResponse;
import com.sanly.medical.entity.PharmacyStatus;
import com.sanly.medical.entity.RegisteredPharmacy;
import com.sanly.medical.repository.RegisteredPharmacyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.HexFormat;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PharmacyManagementService {

    private final RegisteredPharmacyRepository pharmacyRepository;
    private final PasswordEncoder              passwordEncoder;

    @Transactional
    public PharmacyResponse register(RegisterPharmacyRequest req) {
        if (pharmacyRepository.existsByLicenseNumber(req.getLicenseNumber())) {
            throw new IllegalArgumentException("License number already registered: " + req.getLicenseNumber());
        }
        String pharmacyCode = generatePharmacyCode();
        String rawKey       = generateRawKey();

        RegisteredPharmacy pharmacy = RegisteredPharmacy.builder()
                .pharmacyCode(pharmacyCode)
                .name(req.getName())
                .licenseNumber(req.getLicenseNumber())
                .region(req.getRegion())
                .address(req.getAddress())
                .apiKeyHash(passwordEncoder.encode(rawKey))
                .status(PharmacyStatus.ACTIVE)
                .build();

        RegisteredPharmacy saved = pharmacyRepository.save(pharmacy);
        log.info("Registered pharmacy {} ({})", pharmacyCode, req.getName());

        PharmacyResponse response = PharmacyResponse.from(saved);
        response.setRawApiKey(rawKey); // one-time — not stored in plaintext
        return response;
    }

    @Transactional(readOnly = true)
    public List<PharmacyResponse> listAll() {
        return pharmacyRepository.findAllByOrderByRegisteredAtDesc()
                .stream().map(PharmacyResponse::from).toList();
    }

    @Transactional
    public PharmacyResponse updateStatus(String pharmacyCode, PharmacyStatusRequest req) {
        RegisteredPharmacy pharmacy = pharmacyRepository.findByPharmacyCode(pharmacyCode)
                .orElseThrow(() -> new RuntimeException("Pharmacy not found: " + pharmacyCode));
        pharmacy.setStatus(req.getStatus());
        return PharmacyResponse.from(pharmacyRepository.save(pharmacy));
    }

    @Transactional
    public PharmacyResponse rotateKey(String pharmacyCode) {
        RegisteredPharmacy pharmacy = pharmacyRepository.findByPharmacyCode(pharmacyCode)
                .orElseThrow(() -> new RuntimeException("Pharmacy not found: " + pharmacyCode));
        String rawKey = generateRawKey();
        pharmacy.setApiKeyHash(passwordEncoder.encode(rawKey));
        RegisteredPharmacy saved = pharmacyRepository.save(pharmacy);
        log.info("API key rotated for pharmacy {}", pharmacyCode);

        PharmacyResponse response = PharmacyResponse.from(saved);
        response.setRawApiKey(rawKey);
        return response;
    }

    private String generatePharmacyCode() {
        long count = pharmacyRepository.count() + 1;
        return String.format("TM-PHR-%04d", count);
    }

    private String generateRawKey() {
        byte[] bytes = new byte[32];
        new SecureRandom().nextBytes(bytes);
        return HexFormat.of().formatHex(bytes);
    }
}
