package com.sanly.police.config;

import com.sanly.police.entity.PoliceOfficer;
import com.sanly.police.repository.OfficerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private final OfficerRepository officerRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${seed.admin.username}") private String adminUsername;
    @Value("${seed.admin.password}") private String adminPassword;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (!officerRepository.existsByUsername(adminUsername)) {
            officerRepository.save(PoliceOfficer.builder()
                    .username(adminUsername)
                    .password(passwordEncoder.encode(adminPassword))
                    .firstName("Admin").lastName("Police")
                    .roles(Set.of("ROLE_ADMIN"))
                    .build());
            log.info("Seeded admin officer '{}'", adminUsername);
        }
    }
}
