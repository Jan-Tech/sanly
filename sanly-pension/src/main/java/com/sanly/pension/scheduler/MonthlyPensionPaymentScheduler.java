package com.sanly.pension.scheduler;

import com.sanly.pension.client.NotificationClient;
import com.sanly.pension.entity.AccountStatus;
import com.sanly.pension.entity.PaymentStatus;
import com.sanly.pension.entity.PensionAccount;
import com.sanly.pension.entity.PensionPayment;
import com.sanly.pension.repository.PensionAccountRepository;
import com.sanly.pension.repository.PensionPaymentRepository;
import com.sanly.pension.repository.RetirementApplicationRepository;
import com.sanly.pension.entity.ApplicationStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class MonthlyPensionPaymentScheduler {
    private final PensionAccountRepository accountRepo;
    private final PensionPaymentRepository paymentRepo;
    private final RetirementApplicationRepository applicationRepo;
    private final NotificationClient notificationClient;

    @Scheduled(cron = "0 0 7 1 * *")
    public void scheduleMonthlyPayments() {
        String month = currentMonth();
        List<PensionAccount> payingAccounts = accountRepo.findByStatus(AccountStatus.PAYING);
        log.info("[PENSION-SCHEDULER] Scheduling payments for {} PAYING accounts, month={}", payingAccounts.size(), month);

        for (PensionAccount account : payingAccounts) {
            if (paymentRepo.existsByAccountCodeAndPaymentMonth(account.getAccountCode(), month)) continue;

            String monthlyAmount = applicationRepo.findByCitizenNationalIdOrderByAppliedAtDesc(account.getCitizenNationalId())
                    .stream().filter(a -> a.getStatus() == ApplicationStatus.APPROVED)
                    .findFirst().map(a -> a.getMonthlyPensionAmount()).orElse("0.00");

            PensionPayment payment = paymentRepo.save(PensionPayment.builder()
                    .accountCode(account.getAccountCode()).citizenNationalId(account.getCitizenNationalId())
                    .paymentMonth(month).amount(monthlyAmount).status(PaymentStatus.SCHEDULED)
                    .scheduledDate(LocalDate.now().withDayOfMonth(5)).build());

            notificationClient.send(account.getCitizenNationalId(), "PENSION_PAYMENT_SCHEDULED", "EN",
                    Map.of("amount", monthlyAmount, "scheduledDate", payment.getScheduledDate().toString()));
        }
    }

    private String currentMonth() {
        LocalDate now = LocalDate.now();
        return now.getYear() + "-" + String.format("%02d", now.getMonthValue());
    }
}
