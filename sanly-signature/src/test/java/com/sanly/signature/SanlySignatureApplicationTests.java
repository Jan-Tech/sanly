package com.sanly.signature;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
        "spring.flyway.enabled=false",
        "spring.jpa.hibernate.ddl-auto=none",
        "jwt.secret=test-secret-key-32-chars-minimum!!",
        "jwt.expiration-ms=86400000",
        "encryption.key=test-encryption-key-32-charssss",
        "signing.secret=test-signing-secret-32-chars-min!",
        "registry.service-key=test-key",
        "notification.service-key=test-key",
        "bridge.institution-key=test-key",
        "seed.admin.password=admin123",
        "spring.datasource.url=jdbc:postgresql://localhost:5449/sanly_signature",
        "spring.datasource.username=signature_user",
        "spring.datasource.password=test"
})
class SanlySignatureApplicationTests {

    @Test
    void contextLoads() {
        // Verifies that the Spring application context starts correctly
    }
}
