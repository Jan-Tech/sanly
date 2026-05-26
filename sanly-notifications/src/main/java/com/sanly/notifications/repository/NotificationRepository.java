package com.sanly.notifications.repository;

import com.sanly.notifications.entity.EventType;
import com.sanly.notifications.entity.Notification;
import com.sanly.notifications.entity.NotificationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface NotificationRepository extends JpaRepository<Notification, UUID> {

    Page<Notification> findByCitizenNationalIdOrderByCreatedAtDesc(String nationalId, Pageable pageable);

    Page<Notification> findByCitizenNationalIdAndStatusOrderByCreatedAtDesc(
            String nationalId, NotificationStatus status, Pageable pageable);

    Page<Notification> findByCitizenNationalIdAndEventTypeOrderByCreatedAtDesc(
            String nationalId, EventType eventType, Pageable pageable);

    long countByCitizenNationalIdAndStatus(String nationalId, NotificationStatus status);

    List<Notification> findTop5ByCitizenNationalIdOrderByCreatedAtDesc(String nationalId);

    @Modifying
    @Query("UPDATE Notification n SET n.status = 'READ', n.readAt = :now " +
           "WHERE n.citizenNationalId = :nationalId AND n.status = 'UNREAD'")
    int markAllAsRead(@Param("nationalId") String nationalId, @Param("now") LocalDateTime now);

    // Admin queries
    @Query("SELECT n FROM Notification n WHERE " +
           "(:nationalId IS NULL OR n.citizenNationalId = :nationalId) AND " +
           "(:eventType IS NULL OR n.eventType = :eventType) AND " +
           "(:status IS NULL OR n.status = :status) AND " +
           "(:from IS NULL OR n.createdAt >= :from) AND " +
           "(:to IS NULL OR n.createdAt <= :to) " +
           "ORDER BY n.createdAt DESC")
    Page<Notification> findWithFilters(
            @Param("nationalId") String nationalId,
            @Param("eventType") EventType eventType,
            @Param("status") NotificationStatus status,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to,
            Pageable pageable);

    @Query("SELECT n.eventType, n.status, COUNT(n) FROM Notification n " +
           "WHERE n.createdAt >= :since GROUP BY n.eventType, n.status")
    List<Object[]> countByEventTypeAndStatusSince(@Param("since") LocalDateTime since);

    long countByStatusAndCreatedAtAfter(NotificationStatus status, LocalDateTime since);
}
