package com.sanly.banking.config;

import com.sanly.banking.entity.BankingOfficer;
import com.sanly.banking.repository.BankingOfficerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private final BankingOfficerRepository officerRepository;
    private final PasswordEncoder          passwordEncoder;

    @Value("${seed.admin.username}") private String adminUsername;
    @Value("${seed.admin.password}") private String adminPassword;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        // Seed admin officer
        if (officerRepository.findByUsernameAndActiveTrue(adminUsername).isEmpty()) {
            officerRepository.save(BankingOfficer.builder()
                    .username(adminUsername)
                    .passwordHash(passwordEncoder.encode(adminPassword))
                    .role("ROLE_ADMIN")
                    .active(true)
                    .createdAt(LocalDateTime.now())
                    .build());
            log.info("Seeded admin officer '{}'", adminUsername);
        }

        // Warning about demo bank API keys from Flyway seed
        log.warn("SECURITY WARNING: Demo banks were seeded with a default API key hash. " +
                 "All banks must rotate their API keys via POST /api/v1/banking/banks/{bankCode}/rotate-key " +
                 "before production use.");
    }
}
