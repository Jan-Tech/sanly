package com.sanly.vehicle.config;

import com.sanly.vehicle.entity.OfficerRole;
import com.sanly.vehicle.entity.OfficerStatus;
import com.sanly.vehicle.entity.VehicleOfficer;
import com.sanly.vehicle.repository.VehicleOfficerRepository;
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
    private final VehicleOfficerRepository repo;
    private final PasswordEncoder passwordEncoder;
    @Value("${seed.admin.password:admin123}") private String adminPassword;

    @Override
    public void run(String... args) {
        if (repo.findByUsername("admin").isEmpty()) {
            repo.save(VehicleOfficer.builder()
                    .nationalId("SYSTEM-ADMIN")
                    .firstName("Vehicle")
                    .lastName("Administrator")
                    .username("admin")
                    .password(passwordEncoder.encode(adminPassword))
                    .role(OfficerRole.ADMIN)
                    .status(OfficerStatus.ACTIVE)
                    .build());
            log.info("Default admin officer seeded for sanly-vehicle");
        }
    }
}
