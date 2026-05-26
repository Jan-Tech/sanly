package com.sanly.dmv;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@OpenAPIDefinition(
    info = @Info(
        title       = "SANLY DMV Service API",
        version     = "1.0",
        description = "Driving license authority portal. Queries SANLY Bridge for vision test data. "
                    + "GET /api/v1/licenses/verify/{licenseNumber} is PUBLIC (no auth). "
                    + "All other endpoints require Bearer JWT from POST /api/v1/auth/login."
    )
)
@SecurityScheme(
    name         = "bearerAuth",
    type         = SecuritySchemeType.HTTP,
    scheme       = "bearer",
    bearerFormat = "JWT",
    in           = SecuritySchemeIn.HEADER
)
public class DmvApplication {
    public static void main(String[] args) {
        SpringApplication.run(DmvApplication.class, args);
    }
}
