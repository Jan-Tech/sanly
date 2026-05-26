package com.sanly.pharmacy.config;

import com.sanly.pharmacy.entity.PharmacyStaff;
import com.sanly.pharmacy.entity.StaffRole;
import com.sanly.pharmacy.entity.StaffStatus;
import com.sanly.pharmacy.repository.PharmacyStaffRepository;
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

    private final PharmacyStaffRepository staffRepository;
    private final PasswordEncoder         passwordEncoder;

    @Value("${sanly.medical.pharmacy-code:TM-PHR-0001}")
    private String pharmacyCode;

    @Value("${seed.admin.password}")
    private String adminPassword;

    @Override
    public void run(String... args) {
        if (staffRepository.findByUsername("manager").isEmpty()) {
            PharmacyStaff manager = PharmacyStaff.builder()
                    .nationalId("00000000001")
                    .firstName("Pharmacy")
                    .lastName("Manager")
                    .username("manager")
                    .password(passwordEncoder.encode(adminPassword))
                    .pharmacyCode(pharmacyCode)
                    .role(StaffRole.MANAGER)
                    .status(StaffStatus.ACTIVE)
                    .build();
            staffRepository.save(manager);
            log.info("Default manager account seeded for pharmacy {}", pharmacyCode);
        }
    }
}
