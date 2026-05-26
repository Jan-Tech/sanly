package com.sanly.notifications.repository;

import com.sanly.notifications.entity.EventType;
import com.sanly.notifications.entity.NotificationPreference;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface NotificationPreferenceRepository extends JpaRepository<NotificationPreference, UUID> {

    List<NotificationPreference> findByCitizenNationalId(String nationalId);

    Optional<NotificationPreference> findByCitizenNationalIdAndEventType(
            String nationalId, EventType eventType);
}
