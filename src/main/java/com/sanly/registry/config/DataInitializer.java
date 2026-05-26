package com.sanly.registry.config;

import com.sanly.registry.entity.User;
import com.sanly.registry.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

/**
 * Seeds the database with initial admin and institution users on first startup.
 * Credentials are provided via environment variables — never hardcoded.
 * Runs idempotently: existing usernames are skipped.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private final UserRepository  userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${seed.admin.username}")
    private String adminUsername;

    @Value("${seed.admin.password}")
    private String adminPassword;

    @Value("${seed.institution.username}")
    private String institutionUsername;

    @Value("${seed.institution.password}")
    private String institutionPassword;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        createUserIfAbsent(adminUsername,       adminPassword,       "ROLE_ADMIN");
        createUserIfAbsent(institutionUsername, institutionPassword, "ROLE_INSTITUTION");
    }

    private void createUserIfAbsent(String username, String password, String role) {
        if (userRepository.existsByUsername(username)) {
            log.debug("User '{}' already exists — skipping seed", username);
            return;
        }
        User user = User.builder()
                .username(username)
                .password(passwordEncoder.encode(password))
                .roles(Set.of(role))
                .enabled(true)
                .build();
        userRepository.save(user);
        log.info("Seeded user '{}' with role {}", username, role);
    }
}
