package com.sanly.registry.scheduler;

import com.sanly.registry.repository.OtpRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class OtpCleanupScheduler {
    private final OtpRecordRepository otpRecordRepository;

    @Scheduled(fixedDelay = 3_600_000) // every hour
    @Transactional
    public void cleanup() {
        int deleted = otpRecordRepository.deleteOlderThan(LocalDateTime.now().minusHours(24));
        if (deleted > 0) log.info("[OTP-CLEANUP] Deleted {} expired OTP records", deleted);
    }
}
