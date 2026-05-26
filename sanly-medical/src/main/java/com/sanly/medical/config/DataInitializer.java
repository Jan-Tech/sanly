package com.sanly.medical.config;

import com.sanly.medical.entity.Doctor;
import com.sanly.medical.repository.DoctorRepository;
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
 * Seeds the system with a single admin account on first startup.
 * Admin accounts have no clinicId and no nationalId — they are
 * purely administrative users.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private final DoctorRepository doctorRepository;
    private final PasswordEncoder  passwordEncoder;

    @Value("${seed.admin.username}") private String adminUsername;
    @Value("${seed.admin.password}") private String adminPassword;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (!doctorRepository.existsByUsername(adminUsername)) {
            doctorRepository.save(Doctor.builder()
                    .firstName("System")
                    .lastName("Administrator")
                    .username(adminUsername)
                    .password(passwordEncoder.encode(adminPassword))
                    .roles(Set.of("ROLE_ADMIN"))
                    .build());
            log.info("Seeded admin user '{}'", adminUsername);
        }
    }
}
