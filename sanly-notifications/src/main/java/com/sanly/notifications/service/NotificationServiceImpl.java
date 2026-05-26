package com.sanly.notifications.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sanly.notifications.dto.request.SendNotificationRequest;
import com.sanly.notifications.dto.request.UpdatePreferencesRequest;
import com.sanly.notifications.dto.response.NotificationResponse;
import com.sanly.notifications.dto.response.NotificationStatsResponse;
import com.sanly.notifications.dto.response.PageResponse;
import com.sanly.notifications.dto.response.PreferenceResponse;
import com.sanly.notifications.entity.*;
import com.sanly.notifications.exception.NotificationNotFoundException;
import com.sanly.notifications.repository.NotificationPreferenceRepository;
import com.sanly.notifications.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl {

    private final NotificationRepository           notificationRepository;
    private final NotificationPreferenceRepository preferenceRepository;
    private final TemplateService                  templateService;
    private final EmailDeliveryService             emailDeliveryService;
    private final SmsDeliveryService               smsDeliveryService;
    private final ObjectMapper                     objectMapper = new ObjectMapper();

    // ── Send ─────────────────────────────────────────────────────────────────

    @Transactional
    public NotificationResponse send(SendNotificationRequest req) {
        NotificationTemplate template = templateService.resolve(req.getEventType(), req.getLanguage());

        String title = templateService.fill(template.getTitleTemplate(), req.getMetadata());
        String body  = templateService.fill(template.getBodyTemplate(),  req.getMetadata());

        Notification notification = Notification.builder()
                .citizenNationalId(req.getCitizenNationalId())
                .eventType(req.getEventType())
                .title(title)
                .body(body)
                .channel(template.getChannel())
                .language(req.getLanguage())
                .status(NotificationStatus.UNREAD)
                .createdAt(LocalDateTime.now())
                .metadata(serializeMetadata(req.getMetadata()))
                .build();

        notification = notificationRepository.save(notification);

        // SMS dispatch — channel=SMS templates carry phoneNumber in metadata
        if (template.getChannel() == Channel.SMS || template.getChannel() == Channel.ALL) {
            String phone = req.getMetadata() != null ? req.getMetadata().get("phoneNumber") : null;
            if (phone != null && !phone.isBlank()) {
                smsDeliveryService.sendSms(phone, body);
            } else {
                log.debug("SMS channel requested but no phoneNumber in metadata for NIN={}", req.getCitizenNationalId());
            }
        }

        // Email dispatch
        if ((template.getChannel() == Channel.EMAIL || template.getChannel() == Channel.ALL)
                && shouldSendEmail(req.getCitizenNationalId(), req.getEventType())) {
            log.debug("Email delivery skipped — no citizen email lookup in prototype; in-app notification stored");
        }

        return toResponse(notification);
    }

    @Transactional
    public List<NotificationResponse> sendBulk(List<String> nationalIds,
                                                EventType eventType,
                                                Language language,
                                                Map<String, String> metadata) {
        List<NotificationResponse> results = new ArrayList<>();
        for (String nationalId : nationalIds) {
            SendNotificationRequest req = new SendNotificationRequest();
            req.setCitizenNationalId(nationalId);
            req.setEventType(eventType);
            req.setLanguage(language);
            req.setMetadata(metadata);
            try {
                results.add(send(req));
            } catch (Exception ex) {
                log.warn("Bulk send failed for NIN={} event={}: {}", nationalId, eventType, ex.getMessage());
            }
        }
        return results;
    }

    // ── Citizen queries ───────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public PageResponse<NotificationResponse> getMyNotifications(String nationalId,
                                                                  NotificationStatus status,
                                                                  EventType eventType,
                                                                  int page, int size) {
        PageRequest pr = PageRequest.of(page, size);
        Page<Notification> pg;

        if (status != null) {
            pg = notificationRepository.findByCitizenNationalIdAndStatusOrderByCreatedAtDesc(
                    nationalId, status, pr);
        } else if (eventType != null) {
            pg = notificationRepository.findByCitizenNationalIdAndEventTypeOrderByCreatedAtDesc(
                    nationalId, eventType, pr);
        } else {
            pg = notificationRepository.findByCitizenNationalIdOrderByCreatedAtDesc(nationalId, pr);
        }

        Page<NotificationResponse> mapped = pg.map(this::toResponse);
        return PageResponse.<NotificationResponse>builder()
                .content(mapped.getContent())
                .page(mapped.getNumber())
                .size(mapped.getSize())
                .totalElements(mapped.getTotalElements())
                .totalPages(mapped.getTotalPages())
                .last(mapped.isLast())
                .build();
    }

    @Transactional(readOnly = true)
    public long getUnreadCount(String nationalId) {
        return notificationRepository.countByCitizenNationalIdAndStatus(
                nationalId, NotificationStatus.UNREAD);
    }

    @Transactional(readOnly = true)
    public List<NotificationResponse> getRecentFive(String nationalId) {
        return notificationRepository
                .findTop5ByCitizenNationalIdOrderByCreatedAtDesc(nationalId)
                .stream().map(this::toResponse).toList();
    }

    @Transactional
    public NotificationResponse markRead(String nationalId, UUID notificationId) {
        Notification n = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new NotificationNotFoundException(notificationId));

        if (!n.getCitizenNationalId().equals(nationalId)) {
            throw new org.springframework.security.access.AccessDeniedException("Not your notification");
        }
        if (n.getStatus() == NotificationStatus.UNREAD) {
            n.setStatus(NotificationStatus.READ);
            n.setReadAt(LocalDateTime.now());
            n = notificationRepository.save(n);
        }
        return toResponse(n);
    }

    @Transactional
    public int markAllRead(String nationalId) {
        return notificationRepository.markAllAsRead(nationalId, LocalDateTime.now());
    }

    // ── Preferences ───────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<PreferenceResponse> getPreferences(String nationalId) {
        return preferenceRepository.findByCitizenNationalId(nationalId)
                .stream().map(this::toPrefResponse).toList();
    }

    @Transactional
    public PreferenceResponse updatePreference(String nationalId, UpdatePreferencesRequest req) {
        NotificationPreference pref = preferenceRepository
                .findByCitizenNationalIdAndEventType(nationalId, req.getEventType())
                .orElseGet(() -> NotificationPreference.builder()
                        .citizenNationalId(nationalId)
                        .eventType(req.getEventType())
                        .build());

        if (req.getEmailEnabled() != null) pref.setEmailEnabled(req.getEmailEnabled());
        if (req.getSmsEnabled()   != null) pref.setSmsEnabled(req.getSmsEnabled());
        if (req.getInAppEnabled() != null) pref.setInAppEnabled(req.getInAppEnabled());
        if (req.getEnabled()      != null) pref.setEnabled(req.getEnabled());

        return toPrefResponse(preferenceRepository.save(pref));
    }

    // ── Admin ─────────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public PageResponse<NotificationResponse> adminGetAll(String nationalId,
                                                           EventType eventType,
                                                           NotificationStatus status,
                                                           LocalDateTime from,
                                                           LocalDateTime to,
                                                           int page, int size) {
        Page<NotificationResponse> pg = notificationRepository
                .findWithFilters(nationalId, eventType, status, from, to, PageRequest.of(page, size))
                .map(this::toResponse);
        return PageResponse.<NotificationResponse>builder()
                .content(pg.getContent())
                .page(pg.getNumber())
                .size(pg.getSize())
                .totalElements(pg.getTotalElements())
                .totalPages(pg.getTotalPages())
                .last(pg.isLast())
                .build();
    }

    @Transactional(readOnly = true)
    public NotificationStatsResponse getStats() {
        LocalDateTime since = LocalDateTime.now().minusHours(24);

        long totalSentToday   = notificationRepository.count() > 0
                ? notificationRepository.countByStatusAndCreatedAtAfter(NotificationStatus.UNREAD,   since)
                + notificationRepository.countByStatusAndCreatedAtAfter(NotificationStatus.READ,      since)
                + notificationRepository.countByStatusAndCreatedAtAfter(NotificationStatus.DELIVERED, since)
                + notificationRepository.countByStatusAndCreatedAtAfter(NotificationStatus.FAILED,    since)
                : 0;
        long totalFailedToday = notificationRepository.countByStatusAndCreatedAtAfter(NotificationStatus.FAILED, since);
        long totalUnread      = notificationRepository.countByStatusAndCreatedAtAfter(NotificationStatus.UNREAD, LocalDateTime.now().minusYears(10));

        List<Object[]> rows = notificationRepository.countByEventTypeAndStatusSince(since);
        Map<String, Long> byEventType = new LinkedHashMap<>();
        Map<String, Long> byChannel   = new LinkedHashMap<>();

        // Aggregate counts from (eventType, status, count) tuples
        for (Object[] row : rows) {
            String et = row[0].toString();
            byEventType.merge(et, ((Number) row[2]).longValue(), Long::sum);
        }

        return NotificationStatsResponse.builder()
                .totalSentToday(totalSentToday)
                .totalFailedToday(totalFailedToday)
                .totalUnread(totalUnread)
                .byEventType(byEventType)
                .byChannel(byChannel)
                .build();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private boolean shouldSendEmail(String nationalId, EventType eventType) {
        return preferenceRepository
                .findByCitizenNationalIdAndEventType(nationalId, eventType)
                .map(p -> p.isEnabled() && p.isEmailEnabled())
                .orElse(true); // default: send email
    }

    private NotificationResponse toResponse(Notification n) {
        return NotificationResponse.builder()
                .notificationId(n.getNotificationId())
                .citizenNationalId(n.getCitizenNationalId())
                .eventType(n.getEventType())
                .title(n.getTitle())
                .body(n.getBody())
                .channel(n.getChannel())
                .language(n.getLanguage())
                .status(n.getStatus())
                .createdAt(n.getCreatedAt())
                .readAt(n.getReadAt())
                .deliveredAt(n.getDeliveredAt())
                .build();
    }

    private PreferenceResponse toPrefResponse(NotificationPreference p) {
        return PreferenceResponse.builder()
                .preferenceId(p.getPreferenceId())
                .eventType(p.getEventType())
                .emailEnabled(p.isEmailEnabled())
                .smsEnabled(p.isSmsEnabled())
                .inAppEnabled(p.isInAppEnabled())
                .enabled(p.isEnabled())
                .build();
    }

    private String serializeMetadata(Map<String, String> metadata) {
        if (metadata == null || metadata.isEmpty()) return null;
        try { return objectMapper.writeValueAsString(metadata); }
        catch (JsonProcessingException e) { return null; }
    }
}
