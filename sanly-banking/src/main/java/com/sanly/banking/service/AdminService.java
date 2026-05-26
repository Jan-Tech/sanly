package com.sanly.banking.service;

import com.sanly.banking.dto.response.AdminStatsResponse;
import com.sanly.banking.repository.BankDataAccessRepository;
import com.sanly.banking.repository.ConsentRequestRepository;
import com.sanly.banking.repository.RegisteredBankRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final ConsentRequestRepository consentRepo;
    private final BankDataAccessRepository accessRepo;
    private final RegisteredBankRepository bankRepo;

    public AdminStatsResponse getStats() {
        LocalDateTime startOfDay = LocalDateTime.now().toLocalDate().atStartOfDay();

        // Count consents requested today
        long requestedToday = consentRepo.findByStatusAndRequestedAtBefore("PENDING",
                LocalDateTime.now().plusYears(100)).stream()
                .filter(c -> c.getRequestedAt() != null && c.getRequestedAt().isAfter(startOfDay))
                .count();

        // Actually count ALL statuses from today
        List<com.sanly.banking.entity.ConsentRequest> all = consentRepo.findAll();
        long totalToday      = all.stream().filter(c -> c.getRequestedAt() != null && c.getRequestedAt().isAfter(startOfDay)).count();
        long approvedToday   = all.stream().filter(c -> "APPROVED".equals(c.getStatus()) && c.getApprovedAt() != null && c.getApprovedAt().isAfter(startOfDay)).count();
        long rejectedToday   = all.stream().filter(c -> "REJECTED".equals(c.getStatus()) && c.getRejectedAt() != null && c.getRejectedAt().isAfter(startOfDay)).count();
        long expiredToday    = all.stream().filter(c -> "EXPIRED".equals(c.getStatus()) && c.getRequestedAt() != null && c.getRequestedAt().isAfter(startOfDay)).count();
        long activeBanks     = bankRepo.findByStatus("ACTIVE").size();

        return AdminStatsResponse.builder()
                .consentRequestsToday(totalToday)
                .approvedToday(approvedToday)
                .rejectedToday(rejectedToday)
                .expiredToday(expiredToday)
                .totalActiveBanks(activeBanks)
                .build();
    }
}
