package com.sanly.analytics.config;

import com.sanly.analytics.entity.AnalyticsOfficer;
import com.sanly.analytics.repository.AnalyticsOfficerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final AnalyticsOfficerRepository officerRepo;
    private final PasswordEncoder passwordEncoder;

    @Value("${seed.admin.username:admin}")
    private String adminUsername;

    @Value("${seed.admin.password}")
    private String adminPassword;

    @Override
    public void run(String... args) {
        if (officerRepo.findByUsernameAndActiveTrue(adminUsername).isEmpty()) {
            AnalyticsOfficer admin = AnalyticsOfficer.builder()
                    .username(adminUsername)
                    .passwordHash(passwordEncoder.encode(adminPassword))
                    .role("ROLE_ADMIN")
                    .active(true)
                    .build();
            officerRepo.save(admin);
            log.info("Seeded analytics admin officer: {}", adminUsername);
        }
    }
}
