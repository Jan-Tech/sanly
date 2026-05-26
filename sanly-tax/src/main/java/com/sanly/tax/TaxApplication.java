package com.sanly.tax;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.*;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@OpenAPIDefinition(info = @Info(title = "SANLY Tax API", version = "1.0",
        description = "Tax authority. Registers taxpayers (TM-TAX-YYYYNNNNNN), manages filings, publishes TAX_STATUS to SANLY Bridge."))
@SecurityScheme(name = "bearerAuth", type = SecuritySchemeType.HTTP,
        scheme = "bearer", bearerFormat = "JWT", in = SecuritySchemeIn.HEADER)
public class TaxApplication {
    public static void main(String[] args) { SpringApplication.run(TaxApplication.class, args); }
}
