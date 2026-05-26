package com.sanly.notifications.repository;

import com.sanly.notifications.entity.Channel;
import com.sanly.notifications.entity.EventType;
import com.sanly.notifications.entity.Language;
import com.sanly.notifications.entity.NotificationTemplate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface NotificationTemplateRepository extends JpaRepository<NotificationTemplate, UUID> {

    Optional<NotificationTemplate> findByEventTypeAndLanguageAndChannelAndIsActiveTrue(
            EventType eventType, Language language, Channel channel);

    Optional<NotificationTemplate> findByEventTypeAndLanguageAndIsActiveTrue(
            EventType eventType, Language language);

    List<NotificationTemplate> findAllByIsActiveTrueOrderByEventTypeAscLanguageAsc();
}
