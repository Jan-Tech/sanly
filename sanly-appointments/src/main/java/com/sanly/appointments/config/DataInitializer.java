package com.sanly.appointments.config;

import com.sanly.appointments.entity.AppointmentOfficer;
import com.sanly.appointments.entity.OfficerRole;
import com.sanly.appointments.entity.OfficerStatus;
import com.sanly.appointments.repository.AppointmentOfficerRepository;
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

    private final AppointmentOfficerRepository officerRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${seed.admin.username:admin}")
    private String adminUsername;

    @Value("${seed.admin.password}")
    private String adminPassword;

    @Override
    public void run(String... args) {
        if (officerRepository.findByUsername(adminUsername).isEmpty()) {
            AppointmentOfficer admin = AppointmentOfficer.builder()
                    .firstName("System")
                    .lastName("Administrator")
                    .username(adminUsername)
                    .password(passwordEncoder.encode(adminPassword))
                    .role(OfficerRole.ADMIN)
                    .status(OfficerStatus.ACTIVE)
                    .build();
            officerRepository.save(admin);
            log.info("Admin user '{}' seeded successfully.", adminUsername);
        } else {
            log.info("Admin user '{}' already exists, skipping seed.", adminUsername);
        }
    }
}
