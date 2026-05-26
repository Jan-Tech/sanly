package com.sanly.registry.repository;

import com.sanly.registry.entity.ActiveSession;
import com.sanly.registry.entity.SessionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface ActiveSessionRepository extends JpaRepository<ActiveSession, UUID> {
    List<ActiveSession> findByNationalIdAndStatusOrderByCreatedAtDesc(String nationalId, SessionStatus status);

    @Modifying
    @Query("UPDATE ActiveSession s SET s.status = 'REVOKED' WHERE s.nationalId = :nationalId AND s.status = 'ACTIVE'")
    int revokeAllForNationalId(@Param("nationalId") String nationalId);

    @Modifying
    @Query("UPDATE ActiveSession s SET s.status = 'REVOKED' WHERE s.nationalId = :nationalId " +
           "AND s.sessionId != :excludeId AND s.status = 'ACTIVE'")
    int revokeAllExcept(@Param("nationalId") String nationalId, @Param("excludeId") UUID excludeId);

    @Modifying
    @Query("UPDATE ActiveSession s SET s.status = 'REVOKED' WHERE s.status = 'ACTIVE' AND s.expiresAt < :now")
    int expireOldSessions(@Param("now") LocalDateTime now);
}
