package com.sanly.social.scheduler;

import com.sanly.social.client.NotificationClient;
import com.sanly.social.entity.BenefitClaim;
import com.sanly.social.entity.BenefitProgram;
import com.sanly.social.entity.ClaimStatus;
import com.sanly.social.entity.ProgramStatus;
import com.sanly.social.repository.BenefitClaimRepository;
import com.sanly.social.repository.BenefitProgramRepository;
import com.sanly.social.service.BenefitPaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class MonthlyPaymentScheduler {

    private final BenefitClaimRepository claimRepo;
    private final BenefitProgramRepository programRepo;
    private final BenefitPaymentService paymentService;
    private final NotificationClient notificationClient;

    @Scheduled(cron = "0 0 8 1 * *")
    public void scheduleMonthlyPayments() {
        String period = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
        log.info("Monthly payment scheduling started for period {}", period);

        List<BenefitClaim> activeClaims = claimRepo.findByStatus(ClaimStatus.ACTIVE);
        int scheduled = 0;

        for (BenefitClaim claim : activeClaims) {
            try {
                BenefitProgram program = programRepo.findByProgramCode(claim.getProgramCode())
                        .filter(p -> p.getStatus() == ProgramStatus.ACTIVE)
                        .orElse(null);
                if (program == null) continue;

                var payment = paymentService.schedulePaymentForPeriod(claim, program.getMonthlyAmount(), period);
                if (payment != null) {
                    scheduled++;
                    notificationClient.send(claim.getCitizenNationalId(), "BENEFIT_PAYMENT_SCHEDULED", "TK",
                            Map.of("claimCode", claim.getClaimCode(), "period", period,
                                   "amount", program.getMonthlyAmount()));
                }
            } catch (Exception e) {
                log.warn("Failed to schedule payment for claim {}: {}", claim.getClaimCode(), e.getMessage());
            }
        }
        log.info("Monthly payment scheduling done: {} payments created for period {}", scheduled, period);
    }
}
