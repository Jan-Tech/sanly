package com.sanly.registry.repository;

import com.sanly.registry.entity.OtpRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

public interface OtpRecordRepository extends JpaRepository<OtpRecord, UUID> {
    Optional<OtpRecord> findBySessionTokenHash(String sessionTokenHash);

    @Modifying
    @Query("DELETE FROM OtpRecord o WHERE o.createdAt < :cutoff")
    int deleteOlderThan(@Param("cutoff") LocalDateTime cutoff);

    /** Checks for an active lockout for this NIN — used to block Step 1 login if locked. */
    @Query("SELECT COUNT(o) > 0 FROM OtpRecord o WHERE o.nationalId = :nationalId " +
           "AND o.lockedUntil > :now AND o.used = false")
    boolean existsActiveLockout(@Param("nationalId") String nationalId, @Param("now") LocalDateTime now);

    /** Finds the most recent unused, unexpired OTP for a given NIN and purpose (service-to-service verify). */
    @Query("SELECT o FROM OtpRecord o WHERE o.nationalId = :nationalId AND o.purpose = :purpose " +
           "AND o.used = false AND o.expiresAt > :now " +
           "ORDER BY o.createdAt DESC")
    java.util.List<OtpRecord> findActiveByNationalIdAndPurpose(
            @Param("nationalId") String nationalId,
            @Param("purpose") com.sanly.registry.entity.OtpPurpose purpose,
            @Param("now") LocalDateTime now);

    default java.util.Optional<OtpRecord> findLatestActiveByNationalIdAndPurpose(
            String nationalId, com.sanly.registry.entity.OtpPurpose purpose, LocalDateTime now) {
        java.util.List<OtpRecord> list = findActiveByNationalIdAndPurpose(nationalId, purpose, now);
        return list.isEmpty() ? java.util.Optional.empty() : java.util.Optional.of(list.get(0));
    }
}
