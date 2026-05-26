package com.sanly.documents.config;

import com.sanly.documents.entity.DocumentOfficer;
import com.sanly.documents.repository.DocumentOfficerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final DocumentOfficerRepository officerRepository;
    private final PasswordEncoder passwordEncoder;
    private final String adminUsername;
    private final String adminPassword;

    public DataInitializer(DocumentOfficerRepository officerRepository,
                           PasswordEncoder passwordEncoder,
                           @Value("${seed.admin.username:admin}") String adminUsername,
                           @Value("${seed.admin.password}") String adminPassword) {
        this.officerRepository = officerRepository;
        this.passwordEncoder = passwordEncoder;
        this.adminUsername = adminUsername;
        this.adminPassword = adminPassword;
    }

    @Override
    public void run(String... args) {
        if (officerRepository.findByUsername(adminUsername).isEmpty()) {
            DocumentOfficer admin = new DocumentOfficer();
            admin.setUsername(adminUsername);
            admin.setPassword(passwordEncoder.encode(adminPassword));
            admin.setRole("ADMIN");
            admin.setEnabled(true);
            officerRepository.save(admin);
            log.info("Default admin officer seeded: {}", adminUsername);
        }
    }
}
