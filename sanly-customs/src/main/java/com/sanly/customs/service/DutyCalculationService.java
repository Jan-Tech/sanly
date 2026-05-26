package com.sanly.customs.service;

import com.sanly.customs.config.UserDetailsImpl;
import com.sanly.customs.dto.request.CalculateDutiesRequest;
import com.sanly.customs.dto.response.DutyCalculationResponse;
import com.sanly.customs.entity.CustomsDeclaration;
import com.sanly.customs.entity.DeclarationType;
import com.sanly.customs.entity.DutyCalculation;
import com.sanly.customs.exception.RecordNotFoundException;
import com.sanly.customs.repository.CustomsDeclarationRepository;
import com.sanly.customs.repository.DutyCalculationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class DutyCalculationService {

    private static final BigDecimal VAT_RATE = new BigDecimal("15.00");

    private final DutyCalculationRepository calcRepo;
    private final CustomsDeclarationRepository declarationRepo;

    /**
     * HS Code duty rate schedule:
     *   Chapter 22, 24  → 15%  (beverages/alcohol, tobacco)
     *   Chapter 84, 85  →  2%  (machinery, electronics)
     *   Chapter 30      →  0%  (pharmaceuticals)
     *   Chapter 10      →  0%  (cereals, grain)
     *   All others      →  5%  (default)
     * EXPORT / TRANSIT  →  0% duty, no VAT
     */
    public BigDecimal determineDutyRate(String hsCode, DeclarationType declarationType) {
        if (declarationType == DeclarationType.EXPORT || declarationType == DeclarationType.TRANSIT)
            return BigDecimal.ZERO;
        if (hsCode == null || hsCode.isBlank()) return new BigDecimal("5.00");

        String prefix = hsCode.strip().replaceAll("[^0-9]", "");
        if (prefix.length() >= 2) {
            String ch = prefix.substring(0, 2);
            return switch (ch) {
                case "22", "24" -> new BigDecimal("15.00");
                case "84", "85" -> new BigDecimal("2.00");
                case "30", "10" -> BigDecimal.ZERO;
                default         -> new BigDecimal("5.00");
            };
        }
        return new BigDecimal("5.00");
    }

    @Transactional
    public DutyCalculationResponse calculate(String declarationCode, CalculateDutiesRequest req) {
        CustomsDeclaration decl = declarationRepo.findByDeclarationCode(declarationCode)
                .orElseThrow(() -> new RecordNotFoundException("Declaration not found: " + declarationCode));

        BigDecimal declaredValue = parseSafe(decl.getDeclaredValue());
        String hsCode = decl.getHsCode();
        DeclarationType type = decl.getDeclarationType();

        // Determine rate — allow officer override
        BigDecimal dutyRate;
        String overrideJustification = null;
        if (req != null && req.overrideRatePercent() != null) {
            dutyRate = req.overrideRatePercent();
            overrideJustification = req.dutyRateOverride();
            log.info("[CUSTOMS] Duty rate overridden to {}% for {} by officer", dutyRate, declarationCode);
        } else {
            dutyRate = determineDutyRate(hsCode, type);
        }

        // Duty = declaredValue * dutyRate / 100
        BigDecimal dutyAmount = declaredValue.multiply(dutyRate)
                .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);

        // VAT applies to imports only, on (value + duty)
        BigDecimal vatRate = (type == DeclarationType.IMPORT) ? VAT_RATE : BigDecimal.ZERO;
        BigDecimal vatBase = declaredValue.add(dutyAmount);
        BigDecimal vatAmount = vatBase.multiply(vatRate)
                .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);

        BigDecimal totalOwed = dutyAmount.add(vatAmount);

        // Upsert — recalculate if already exists
        DutyCalculation existing = calcRepo.findByDeclarationCode(declarationCode).orElse(null);
        DutyCalculation calc;
        if (existing != null) {
            existing.setHsCode(hsCode);
            existing.setDutyRatePercent(dutyRate);
            existing.setVatRatePercent(vatRate);
            existing.setCalculatedDutyAmount(dutyAmount.toPlainString());
            existing.setVatAmount(vatAmount.toPlainString());
            existing.setTotalOwed(totalOwed.toPlainString());
            existing.setDutyRateOverride(overrideJustification);
            existing.setCalculatedByOfficerId(getOfficerId());
            calc = calcRepo.save(existing);
        } else {
            calc = calcRepo.save(DutyCalculation.builder()
                    .declarationCode(declarationCode)
                    .hsCode(hsCode)
                    .dutyRatePercent(dutyRate)
                    .vatRatePercent(vatRate)
                    .calculatedDutyAmount(dutyAmount.toPlainString())
                    .vatAmount(vatAmount.toPlainString())
                    .totalOwed(totalOwed.toPlainString())
                    .dutyRateOverride(overrideJustification)
                    .calculatedByOfficerId(getOfficerId())
                    .build());
        }

        // Update dutiesOwed on declaration
        decl.setDutiesOwed(totalOwed.toPlainString());
        declarationRepo.save(decl);

        return DutyCalculationResponse.from(calc);
    }

    public DutyCalculationResponse getByDeclaration(String declarationCode) {
        return calcRepo.findByDeclarationCode(declarationCode)
                .map(DutyCalculationResponse::from)
                .orElseThrow(() -> new RecordNotFoundException("No duty calculation for: " + declarationCode));
    }

    private BigDecimal parseSafe(String value) {
        try { return new BigDecimal(value == null ? "0" : value); }
        catch (NumberFormatException e) { return BigDecimal.ZERO; }
    }

    private UUID getOfficerId() {
        try {
            Object p = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if (p instanceof UserDetailsImpl ud) return ud.getOfficerId();
        } catch (Exception ignored) {}
        return null;
    }
}
