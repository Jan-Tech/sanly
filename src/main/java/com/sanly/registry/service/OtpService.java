package com.sanly.registry.service;

import com.sanly.registry.client.NotificationClient;
import com.sanly.registry.entity.OtpPurpose;
import com.sanly.registry.entity.OtpRecord;
import com.sanly.registry.repository.OtpRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class OtpService {

    private final OtpRecordRepository otpRecordRepository;
    private final NotificationClient  notificationClient;
    private final PasswordEncoder     passwordEncoder;
    private final SecureRandom        secureRandom = new SecureRandom();

    @Value("${otp.expiry-minutes:5}")        private int expiryMinutes;
    @Value("${otp.max-attempts:3}")          private int maxAttempts;
    @Value("${otp.lockout-minutes:15}")      private int lockoutMinutes;
    @Value("${otp.max-resends:3}")           private int maxResends;

    /**
     * Creates a new OTP record, sends SMS, returns sessionToken to return to client.
     * Caller must be sure the citizen has a phoneNumber before calling.
     */
    @Transactional
    public String createAndSend(String nationalId, String phoneNumber, String language, String ipAddress) {
        if (otpRecordRepository.existsActiveLockout(nationalId, LocalDateTime.now())) {
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS,
                    "Account temporarily locked due to too many failed OTP attempts. Try again later.");
        }

        String otpCode = String.format("%06d", secureRandom.nextInt(1_000_000));
        String sessionToken = generateSessionToken(nationalId);
        String sessionTokenHash = sha256(sessionToken);

        OtpRecord record = OtpRecord.builder()
                .nationalId(nationalId)
                .sessionTokenHash(sessionTokenHash)
                .otpHash(passwordEncoder.encode(otpCode))
                .purpose(OtpPurpose.LOGIN)
                .phoneLastFour(phoneNumber.length() >= 4
                        ? phoneNumber.substring(phoneNumber.length() - 4) : phoneNumber)
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusMinutes(expiryMinutes))
                .ipAddress(ipAddress)
                .build();
        otpRecordRepository.save(record);

        notificationClient.sendOtpSms(nationalId, phoneNumber, otpCode, language);
        return sessionToken;
    }

    /**
     * Verifies OTP. Returns true on success. Increments attempts and handles lockout on failure.
     * Marks record as used on success.
     */
    @Transactional
    public void verify(String sessionToken, String otpCode) {
        OtpRecord record = findActiveRecord(sessionToken);

        if (record.getAttempts() >= maxAttempts) {
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS,
                    "Too many failed attempts. Account locked for " + lockoutMinutes + " minutes.");
        }

        boolean matches = passwordEncoder.matches(otpCode, record.getOtpHash());
        if (!matches) {
            record.setAttempts(record.getAttempts() + 1);
            if (record.getAttempts() >= maxAttempts) {
                record.setLockedUntil(LocalDateTime.now().plusMinutes(lockoutMinutes));
                log.warn("OTP lockout triggered for NIN={}", record.getNationalId());
            }
            otpRecordRepository.save(record);
            int remaining = maxAttempts - record.getAttempts();
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                    remaining > 0 ? "Invalid OTP. " + remaining + " attempt(s) remaining." : "Account locked.");
        }

        record.setUsed(true);
        otpRecordRepository.save(record);
    }

    /** Returns the nationalId from the session token without consuming it. */
    @Transactional(readOnly = true)
    public String getNationalId(String sessionToken) {
        return findActiveRecord(sessionToken).getNationalId();
    }

    @Transactional(readOnly = true)
    public String getPhoneLastFour(String sessionToken) {
        return findActiveRecord(sessionToken).getPhoneLastFour();
    }

    @Transactional(readOnly = true)
    public int getAttemptsRemaining(String sessionToken) {
        OtpRecord r = otpRecordRepository.findBySessionTokenHash(sha256(sessionToken)).orElse(null);
        if (r == null) return maxAttempts;
        return Math.max(0, maxAttempts - r.getAttempts());
    }

    /**
     * Creates and sends an OTP for a specific purpose (e.g. SENSITIVE_ACTION for signing).
     * Unlike createAndSend, this does not return a sessionToken — the caller is already
     * authenticated via JWT and passes nationalId + phone directly.
     */
    @Transactional
    public void createAndSendForPurpose(String nationalId, String phoneNumber,
                                        String language, String ipAddress, OtpPurpose purpose) {
        if (otpRecordRepository.existsActiveLockout(nationalId, LocalDateTime.now())) {
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS,
                    "Account temporarily locked due to too many failed OTP attempts.");
        }

        String otpCode = String.format("%06d", secureRandom.nextInt(1_000_000));
        String sessionToken = generateSessionToken(nationalId);
        String sessionTokenHash = sha256(sessionToken);

        OtpRecord record = OtpRecord.builder()
                .nationalId(nationalId)
                .sessionTokenHash(sessionTokenHash)
                .otpHash(passwordEncoder.encode(otpCode))
                .purpose(purpose)
                .phoneLastFour(phoneNumber.length() >= 4
                        ? phoneNumber.substring(phoneNumber.length() - 4) : phoneNumber)
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusMinutes(expiryMinutes))
                .ipAddress(ipAddress)
                .build();
        otpRecordRepository.save(record);
        notificationClient.sendOtpSms(nationalId, phoneNumber, otpCode, language);
    }

    /**
     * Verifies an OTP for a specific purpose by nationalId (service-to-service call).
     * Returns true if valid and marks the OTP as used. Returns false on failure.
     */
    @Transactional
    public boolean verifyForPurpose(String nationalId, String otpCode, OtpPurpose purpose) {
        OtpRecord record = otpRecordRepository
                .findLatestActiveByNationalIdAndPurpose(nationalId, purpose, LocalDateTime.now())
                .orElse(null);

        if (record == null) return false;
        if (record.getAttempts() >= maxAttempts) return false;

        boolean matches = passwordEncoder.matches(otpCode, record.getOtpHash());
        if (!matches) {
            record.setAttempts(record.getAttempts() + 1);
            if (record.getAttempts() >= maxAttempts) {
                record.setLockedUntil(LocalDateTime.now().plusMinutes(lockoutMinutes));
            }
            otpRecordRepository.save(record);
            return false;
        }

        record.setUsed(true);
        otpRecordRepository.save(record);
        return true;
    }

    /** Resends OTP for an existing session, regenerating the code. */
    @Transactional
    public void resend(String sessionToken, String phoneNumber, String language) {
        OtpRecord record = findActiveRecord(sessionToken);
        if (record.getResendCount() >= maxResends)
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "Maximum resend limit reached.");

        String newOtp = String.format("%06d", secureRandom.nextInt(1_000_000));
        record.setOtpHash(passwordEncoder.encode(newOtp));
        record.setAttempts(0);
        record.setExpiresAt(LocalDateTime.now().plusMinutes(expiryMinutes));
        record.setResendCount(record.getResendCount() + 1);
        otpRecordRepository.save(record);

        notificationClient.sendOtpSms(record.getNationalId(), phoneNumber, newOtp, language);
    }

    private OtpRecord findActiveRecord(String sessionToken) {
        OtpRecord record = otpRecordRepository.findBySessionTokenHash(sha256(sessionToken))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                        "Invalid or expired session token."));
        if (record.isUsed())
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "OTP already used.");
        if (LocalDateTime.now().isAfter(record.getExpiresAt()))
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "OTP expired. Please log in again.");
        if (record.getLockedUntil() != null && LocalDateTime.now().isBefore(record.getLockedUntil()))
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS,
                    "Account temporarily locked due to too many failed attempts.");
        return record;
    }

    private String generateSessionToken(String nationalId) {
        byte[] randomBytes = new byte[32];
        secureRandom.nextBytes(randomBytes);
        String raw = nationalId + System.nanoTime() + UUID.randomUUID();
        return sha256(raw + HexFormat.of().formatHex(randomBytes));
    }

    public static String sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (Exception e) {
            throw new IllegalStateException("SHA-256 unavailable", e);
        }
    }
}
