package com.sanly.notifications.controller;

import com.sanly.notifications.dto.request.BulkSendRequest;
import com.sanly.notifications.dto.request.SendNotificationRequest;
import com.sanly.notifications.dto.response.NotificationResponse;
import com.sanly.notifications.service.NotificationServiceImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Service-to-service endpoints. Authentication via X-Service-Name + X-Service-Key headers
 * (validated by ServiceKeyAuthFilter before reaching this controller).
 */
@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class SendController {

    private final NotificationServiceImpl notificationService;

    @PostMapping("/send")
    @PreAuthorize("hasRole('SERVICE')")
    public ResponseEntity<NotificationResponse> send(@Valid @RequestBody SendNotificationRequest req) {
        return ResponseEntity.ok(notificationService.send(req));
    }

    @PostMapping("/send-bulk")
    @PreAuthorize("hasRole('SERVICE')")
    public ResponseEntity<List<NotificationResponse>> sendBulk(@Valid @RequestBody BulkSendRequest req) {
        List<NotificationResponse> results = notificationService.sendBulk(
                req.getCitizenNationalIds(), req.getEventType(), req.getLanguage(), req.getMetadata());
        return ResponseEntity.ok(results);
    }
}
