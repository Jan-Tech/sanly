package com.sanly.civil.config;

import com.sanly.civil.entity.CivilOfficer;
import com.sanly.civil.entity.OfficerStatus;
import com.sanly.civil.repository.OfficerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class DataInitializer implements CommandLineRunner {
    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final OfficerRepository officerRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(OfficerRepository officerRepository, PasswordEncoder passwordEncoder) {
        this.officerRepository = officerRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (officerRepository.findByUsername("admin").isEmpty()) {
            CivilOfficer admin = new CivilOfficer();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setFullName("System Administrator");
            admin.setStatus(OfficerStatus.ACTIVE);
            admin.setNationalId(null);
            admin.setOfficeRegion(null);
            admin.setRoles(Set.of("ROLE_ADMIN"));
            officerRepository.save(admin);
            log.info("Default admin officer seeded");
        }
    }
}
