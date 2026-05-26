package com.sanly.police;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.*;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@OpenAPIDefinition(info = @Info(title = "SANLY Police API", version = "1.0",
        description = "Police records portal. Publishes CRIMINAL_RECORD to bridge; queries DRIVING_LICENSE and TAX_STATUS."))
@SecurityScheme(name = "bearerAuth", type = SecuritySchemeType.HTTP,
        scheme = "bearer", bearerFormat = "JWT", in = SecuritySchemeIn.HEADER)
public class PoliceApplication {
    public static void main(String[] args) { SpringApplication.run(PoliceApplication.class, args); }
}
