package com.sanly.social.config;

import com.sanly.social.entity.SocialOfficer;
import com.sanly.social.entity.OfficerRole;
import com.sanly.social.entity.OfficerStatus;
import com.sanly.social.repository.SocialOfficerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {
    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);
    private final SocialOfficerRepository repo;
    private final PasswordEncoder passwordEncoder;
    @Value("${seed.admin.password:admin123}") private String adminPassword;

    public DataInitializer(SocialOfficerRepository repo, PasswordEncoder passwordEncoder) {
        this.repo = repo; this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (repo.findByUsername("admin").isEmpty()) {
            repo.save(SocialOfficer.builder()
                    .nationalId("SYSTEM-ADMIN")
                    .firstName("Social")
                    .lastName("Administrator")
                    .username("admin")
                    .password(passwordEncoder.encode(adminPassword))
                    .role(OfficerRole.ADMIN)
                    .status(OfficerStatus.ACTIVE)
                    .build());
            log.info("Default admin officer seeded for sanly-social");
        }
    }
}
