package com.sanly.notifications.controller;

import com.sanly.notifications.dto.request.UpdateTemplateRequest;
import com.sanly.notifications.dto.response.*;
import com.sanly.notifications.entity.EventType;
import com.sanly.notifications.entity.NotificationStatus;
import com.sanly.notifications.service.NotificationServiceImpl;
import com.sanly.notifications.service.TemplateAdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/notifications/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminNotificationController {

    private final NotificationServiceImpl notificationService;
    private final TemplateAdminService    templateAdminService;

    @GetMapping("/all")
    public ResponseEntity<PageResponse<NotificationResponse>> getAll(
            @RequestParam(required = false) String nationalId,
            @RequestParam(required = false) EventType eventType,
            @RequestParam(required = false) NotificationStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        return ResponseEntity.ok(notificationService.adminGetAll(
                nationalId, eventType, status, from, to, page, size));
    }

    @GetMapping("/stats")
    public ResponseEntity<NotificationStatsResponse> getStats() {
        return ResponseEntity.ok(notificationService.getStats());
    }

    @GetMapping("/templates")
    public ResponseEntity<List<TemplateResponse>> listTemplates() {
        return ResponseEntity.ok(templateAdminService.listAll());
    }

    @PutMapping("/templates/{templateId}")
    public ResponseEntity<TemplateResponse> updateTemplate(
            @PathVariable UUID templateId,
            @Valid @RequestBody UpdateTemplateRequest req) {
        return ResponseEntity.ok(templateAdminService.update(templateId, req));
    }
}
