package com.sanly.banking.service;

import com.sanly.banking.dto.response.ScopeDescription;
import com.sanly.banking.entity.ConsentScope;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class ScopeDescriptionMapper {

    private static final Map<ConsentScope, ScopeDescription> DESCRIPTIONS = Map.ofEntries(
            Map.entry(ConsentScope.IDENTITY_BASIC, ScopeDescription.builder()
                    .scope("IDENTITY_BASIC")
                    .name("Basic Identity")
                    .description("Your name, date of birth, and national ID number (masked)")
                    .build()),
            Map.entry(ConsentScope.IDENTITY_FULL, ScopeDescription.builder()
                    .scope("IDENTITY_FULL")
                    .name("Full Identity")
                    .description("Your full name, date of birth, address, and masked phone number")
                    .build()),
            Map.entry(ConsentScope.TAX_STATUS, ScopeDescription.builder()
                    .scope("TAX_STATUS")
                    .name("Tax Status")
                    .description("Whether your taxes are up-to-date (compliant or non-compliant)")
                    .build()),
            Map.entry(ConsentScope.TAX_INCOME_CLASS, ScopeDescription.builder()
                    .scope("TAX_INCOME_CLASS")
                    .name("Income Classification")
                    .description("Your income bracket (low/medium/high) — not exact salary")
                    .build()),
            Map.entry(ConsentScope.CRIMINAL_CLEARANCE, ScopeDescription.builder()
                    .scope("CRIMINAL_CLEARANCE")
                    .name("Criminal Clearance")
                    .description("Whether you have any criminal record (yes/no only, no details)")
                    .build()),
            Map.entry(ConsentScope.BUSINESS_OWNERSHIP, ScopeDescription.builder()
                    .scope("BUSINESS_OWNERSHIP")
                    .name("Business Ownership")
                    .description("List of businesses registered under your name")
                    .build()),
            Map.entry(ConsentScope.PROPERTY_OWNERSHIP, ScopeDescription.builder()
                    .scope("PROPERTY_OWNERSHIP")
                    .name("Property Ownership")
                    .description("Number of properties owned and total value range")
                    .build()),
            Map.entry(ConsentScope.PENSION_STATUS, ScopeDescription.builder()
                    .scope("PENSION_STATUS")
                    .name("Pension Status")
                    .description("Your pension account status and estimated monthly pension range")
                    .build()),
            Map.entry(ConsentScope.EMPLOYMENT_STATUS, ScopeDescription.builder()
                    .scope("EMPLOYMENT_STATUS")
                    .name("Employment Status")
                    .description("Whether you have active pension contributions (employment indicator)")
                    .build()),
            Map.entry(ConsentScope.MEDICAL_CLEARANCE, ScopeDescription.builder()
                    .scope("MEDICAL_CLEARANCE")
                    .name("Medical Clearance")
                    .description("Whether you have any serious medical conditions on record (yes/no only)")
                    .build()),
            Map.entry(ConsentScope.DRIVING_LICENSE, ScopeDescription.builder()
                    .scope("DRIVING_LICENSE")
                    .name("Driving License")
                    .description("Your driving license status, categories, and expiry date")
                    .build())
    );

    public ScopeDescription getDescription(ConsentScope scope) {
        return DESCRIPTIONS.getOrDefault(scope, ScopeDescription.builder()
                .scope(scope.name())
                .name(scope.name())
                .description("No description available")
                .build());
    }
}
