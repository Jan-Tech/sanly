package com.sanly.education.config;

import com.sanly.education.entity.EducationOfficer;
import com.sanly.education.entity.OfficerRole;
import com.sanly.education.entity.OfficerStatus;
import com.sanly.education.repository.EducationOfficerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {
    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final EducationOfficerRepository officerRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${seed.admin.password:admin123}")
    private String adminPassword;

    public DataInitializer(EducationOfficerRepository officerRepository,
                           PasswordEncoder passwordEncoder) {
        this.officerRepository = officerRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (officerRepository.findByUsername("admin").isEmpty()) {
            EducationOfficer admin = EducationOfficer.builder()
                    .nationalId("SYSTEM-ADMIN")
                    .firstName("Education")
                    .lastName("Administrator")
                    .username("admin")
                    .password(passwordEncoder.encode(adminPassword))
                    .role(OfficerRole.ADMIN)
                    .status(OfficerStatus.ACTIVE)
                    .build();
            officerRepository.save(admin);
            log.info("Default admin officer seeded for sanly-education");
        }
    }
}
