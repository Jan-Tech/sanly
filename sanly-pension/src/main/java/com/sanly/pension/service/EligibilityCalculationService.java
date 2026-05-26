package com.sanly.pension.service;

import com.sanly.pension.dto.response.EligibilityResponse;
import com.sanly.pension.entity.PensionAccount;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Service
public class EligibilityCalculationService {

    /**
     * Derives gender from first NIN digit: 1,3,5 = MALE (retirement 60), 2,4,6 = FEMALE (retirement 55).
     */
    public static String deriveGender(String nationalId) {
        char d = nationalId.charAt(0);
        return "135".indexOf(d) >= 0 ? "M" : "F";
    }

    public static int retirementAge(String gender) {
        return "M".equals(gender) ? 60 : 55;
    }

    /**
     * eligibleAt = max(birthDate + retirementAge, employmentStartDate + 15 years)
     */
    public static LocalDate calculateEligibleAt(LocalDate birthDate, String gender, LocalDate employmentStartDate) {
        LocalDate byAge = birthDate.plusYears(retirementAge(gender));
        LocalDate byContributions = employmentStartDate.plusYears(15);
        return byAge.isAfter(byContributions) ? byAge : byContributions;
    }

    public EligibilityResponse calculate(PensionAccount account, long monthsContributed, BigDecimal totalContributions) {
        LocalDate today = LocalDate.now();
        LocalDate eligibleAt = account.getEligibleAt();

        boolean eligible = !today.isBefore(eligibleAt) && monthsContributed >= 180;
        int yearsRemaining = (int) Math.max(0, ChronoUnit.YEARS.between(today, eligibleAt));

        BigDecimal projectedMonthly = BigDecimal.ZERO;
        if (monthsContributed > 0 && totalContributions != null && totalContributions.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal multiplier = monthsContributed >= 300
                    ? new BigDecimal("1.2") : new BigDecimal("0.8");
            projectedMonthly = totalContributions
                    .divide(BigDecimal.valueOf(monthsContributed), 2, RoundingMode.HALF_UP)
                    .multiply(multiplier);
        }
        return new EligibilityResponse(eligible, eligibleAt, yearsRemaining, monthsContributed, projectedMonthly);
    }
}
