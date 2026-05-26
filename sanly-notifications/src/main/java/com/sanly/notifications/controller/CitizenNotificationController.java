package com.sanly.notifications.controller;

import com.sanly.notifications.config.UserDetailsImpl;
import com.sanly.notifications.dto.request.UpdatePreferencesRequest;
import com.sanly.notifications.dto.response.NotificationResponse;
import com.sanly.notifications.dto.response.PageResponse;
import com.sanly.notifications.dto.response.PreferenceResponse;
import com.sanly.notifications.dto.response.UnreadCountResponse;
import com.sanly.notifications.entity.EventType;
import com.sanly.notifications.entity.NotificationStatus;
import com.sanly.notifications.service.NotificationServiceImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/notifications/my")
@RequiredArgsConstructor
public class CitizenNotificationController {

    private final NotificationServiceImpl notificationService;

    @GetMapping
    public ResponseEntity<PageResponse<NotificationResponse>> getMyNotifications(
            @AuthenticationPrincipal UserDetailsImpl principal,
            @RequestParam(required = false) NotificationStatus status,
            @RequestParam(required = false) EventType eventType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(notificationService.getMyNotifications(
                principal.getNationalId(), status, eventType, page, size));
    }

    @GetMapping("/unread-count")
    public ResponseEntity<UnreadCountResponse> getUnreadCount(
            @AuthenticationPrincipal UserDetailsImpl principal) {
        return ResponseEntity.ok(
                new UnreadCountResponse(notificationService.getUnreadCount(principal.getNationalId())));
    }

    @GetMapping("/recent")
    public ResponseEntity<List<NotificationResponse>> getRecentFive(
            @AuthenticationPrincipal UserDetailsImpl principal) {
        return ResponseEntity.ok(notificationService.getRecentFive(principal.getNationalId()));
    }

    @PatchMapping("/{notificationId}/read")
    public ResponseEntity<NotificationResponse> markRead(
            @AuthenticationPrincipal UserDetailsImpl principal,
            @PathVariable UUID notificationId) {
        return ResponseEntity.ok(notificationService.markRead(principal.getNationalId(), notificationId));
    }

    @PatchMapping("/read-all")
    public ResponseEntity<Map<String, Integer>> markAllRead(
            @AuthenticationPrincipal UserDetailsImpl principal) {
        int updated = notificationService.markAllRead(principal.getNationalId());
        return ResponseEntity.ok(Map.of("updated", updated));
    }

    @GetMapping("/preferences")
    public ResponseEntity<List<PreferenceResponse>> getPreferences(
            @AuthenticationPrincipal UserDetailsImpl principal) {
        return ResponseEntity.ok(notificationService.getPreferences(principal.getNationalId()));
    }

    @PutMapping("/preferences")
    public ResponseEntity<PreferenceResponse> updatePreference(
            @AuthenticationPrincipal UserDetailsImpl principal,
            @Valid @RequestBody UpdatePreferencesRequest req) {
        return ResponseEntity.ok(notificationService.updatePreference(principal.getNationalId(), req));
    }
}
