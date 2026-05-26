package com.sanly.land.config;

import com.sanly.land.entity.LandOfficer;
import com.sanly.land.entity.OfficerRole;
import com.sanly.land.entity.OfficerStatus;
import com.sanly.land.repository.LandOfficerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {
    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final LandOfficerRepository officerRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${seed.admin.password:admin123}")
    private String adminPassword;

    public DataInitializer(LandOfficerRepository r, PasswordEncoder p) {
        this.officerRepository = r;
        this.passwordEncoder = p;
    }

    @Override
    public void run(String... args) {
        if (officerRepository.findByUsername("admin").isEmpty()) {
            officerRepository.save(LandOfficer.builder()
                    .nationalId("SYSTEM-ADMIN")
                    .firstName("Land")
                    .lastName("Administrator")
                    .username("admin")
                    .password(passwordEncoder.encode(adminPassword))
                    .role(OfficerRole.ADMIN)
                    .status(OfficerStatus.ACTIVE)
                    .build());
            log.info("Default admin officer seeded for sanly-land");
        }
    }
}
