package com.sanly.pension.config;

import com.sanly.pension.entity.OfficerRole;
import com.sanly.pension.entity.OfficerStatus;
import com.sanly.pension.entity.PensionOfficer;
import com.sanly.pension.repository.PensionOfficerRepository;
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
    private final PensionOfficerRepository repo;
    private final PasswordEncoder passwordEncoder;
    @Value("${seed.admin.password:admin123}") private String adminPassword;

    @Override
    public void run(String... args) {
        if (repo.findByUsername("admin").isEmpty()) {
            repo.save(PensionOfficer.builder()
                    .nationalId("SYSTEM-ADMIN")
                    .firstName("Pension")
                    .lastName("Administrator")
                    .username("admin")
                    .password(passwordEncoder.encode(adminPassword))
                    .role(OfficerRole.ADMIN)
                    .status(OfficerStatus.ACTIVE)
                    .build());
            log.info("Default admin officer seeded for sanly-pension");
        }
    }
}
