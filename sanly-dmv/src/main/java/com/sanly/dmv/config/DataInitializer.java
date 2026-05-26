package com.sanly.dmv.config;

import com.sanly.dmv.entity.DmvOfficer;
import com.sanly.dmv.repository.OfficerRepository;
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
    private final PasswordEncoder   passwordEncoder;

    @Value("${seed.admin.username}") private String adminUsername;
    @Value("${seed.admin.password}") private String adminPassword;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (!officerRepository.existsByUsername(adminUsername)) {
            officerRepository.save(DmvOfficer.builder()
                    .firstName("System").lastName("Administrator")
                    .username(adminUsername)
                    .password(passwordEncoder.encode(adminPassword))
                    .roles(Set.of("ROLE_ADMIN"))
                    .build());
            log.info("Seeded admin user '{}'", adminUsername);
        }
    }
}
