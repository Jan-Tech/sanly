package com.sanly.bridge.config;

import com.sanly.bridge.entity.User;
import com.sanly.bridge.repository.UserRepository;
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

    private final UserRepository  userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${seed.admin.username}")  private String adminUsername;
    @Value("${seed.admin.password}")  private String adminPassword;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (!userRepository.existsByUsername(adminUsername)) {
            userRepository.save(User.builder()
                    .username(adminUsername)
                    .password(passwordEncoder.encode(adminPassword))
                    .roles(Set.of("ROLE_ADMIN"))
                    .enabled(true)
                    .build());
            log.info("Seeded admin user '{}'", adminUsername);
        }
    }
}
