package com.sanly.notifications.config;

import com.sanly.notifications.entity.ServiceAccount;
import com.sanly.notifications.repository.ServiceAccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Seeds a placeholder service account so the service starts up without manual setup.
 * In production, register each calling service via an admin API and rotate the key.
 * The actual plaintext key is set via NOTIFICATION_SERVICE_KEY env var in each caller.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final ServiceAccountRepository serviceAccountRepository;
    private final PasswordEncoder          passwordEncoder;

    @Override
    public void run(String... args) {
        seedIfAbsent("SANLY_BRIDGE",    "bridge-default-key-change-me");
        seedIfAbsent("SANLY_DMV",       "dmv-default-key-change-me");
        seedIfAbsent("SANLY_POLICE",    "police-default-key-change-me");
        seedIfAbsent("SANLY_TAX",       "tax-default-key-change-me");
        seedIfAbsent("SANLY_BUSINESS",  "business-default-key-change-me");
        seedIfAbsent("SANLY_CIVIL",     "civil-default-key-change-me");
        seedIfAbsent("SANLY_MEDICAL",   "medical-default-key-change-me");
        seedIfAbsent("SANLY_PHARMACY",  "pharmacy-default-key-change-me");
        seedIfAbsent("SANLY_REGISTRY",  "registry-default-key-change-me");
        seedIfAbsent("SANLY_EDUCATION", "education-default-key-change-me");
        seedIfAbsent("SANLY_LAND",      "land-default-key-change-me");
        seedIfAbsent("SANLY_SOCIAL",    "social-default-key-change-me");
        seedIfAbsent("SANLY_CUSTOMS",   "customs-default-key-change-me");
        seedIfAbsent("SANLY_COURT",     "court-default-key-change-me");
        seedIfAbsent("SANLY_VEHICLE",    "vehicle-default-key-change-me");
        seedIfAbsent("SANLY_PENSION",   "pension-default-key-change-me");
        seedIfAbsent("SANLY_SIGNATURE",     "signature-default-key-change-me");
        seedIfAbsent("SANLY_APPOINTMENTS", "appointments-default-key-change-me");
        seedIfAbsent("SANLY_DOCUMENTS",    "documents-default-key-change-me");
        seedIfAbsent("SANLY_ANALYTICS",   "analytics-default-key-change-me");
        seedIfAbsent("SANLY_BANKING",     "banking-default-key-change-me");
    }

    private void seedIfAbsent(String name, String defaultKey) {
        if (serviceAccountRepository.findByServiceNameAndActiveTrue(name).isEmpty()) {
            ServiceAccount account = new ServiceAccount();
            account.setServiceName(name);
            account.setKeyHash(passwordEncoder.encode(defaultKey));
            account.setActive(true);
            serviceAccountRepository.save(account);
            log.warn("Seeded default service account '{}' — replace key before production use", name);
        }
    }
}
