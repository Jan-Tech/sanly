package com.sanly.notifications.service;

import com.sanly.notifications.entity.Notification;
import com.sanly.notifications.entity.NotificationStatus;
import com.sanly.notifications.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.mail.internet.MimeMessage;
import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailDeliveryService {

    private final JavaMailSender          mailSender;
    private final NotificationRepository  notificationRepository;

    /**
     * Sends an HTML email for the given notification and updates its status.
     * Runs @Async — never blocks the API response.
     * On failure: marks notification FAILED, logs the error, does not throw.
     */
    @Async("emailExecutor")
    @Transactional
    public void sendEmail(UUID notificationId, String toAddress) {
        Notification notification = notificationRepository.findById(notificationId).orElse(null);
        if (notification == null) return;

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");
            helper.setTo(toAddress);
            helper.setSubject(notification.getTitle());
            helper.setText(buildHtml(notification.getTitle(), notification.getBody()), true);

            mailSender.send(message);

            notification.setStatus(NotificationStatus.DELIVERED);
            notification.setDeliveredAt(LocalDateTime.now());
            notificationRepository.save(notification);

            log.info("Email delivered: notificationId={} to={}", notificationId, toAddress);

        } catch (Exception ex) {
            log.error("Email delivery failed: notificationId={} to={} error={}",
                    notificationId, toAddress, ex.getMessage());
            notification.setStatus(NotificationStatus.FAILED);
            notificationRepository.save(notification);
        }
    }

    private String buildHtml(String title, String body) {
        return "<html><body style=\"font-family:Arial,sans-serif;max-width:600px;margin:0 auto;padding:20px\">"
             + "<div style=\"background:#0D7377;padding:16px 24px;border-radius:8px 8px 0 0\">"
             + "<h2 style=\"color:#fff;margin:0\">SANLY e-Government</h2>"
             + "</div>"
             + "<div style=\"border:1px solid #e0e0e0;border-top:none;padding:24px;border-radius:0 0 8px 8px\">"
             + "<h3 style=\"color:#1A1A2E;margin-top:0\">" + title + "</h3>"
             + "<p style=\"color:#444;line-height:1.6\">" + body + "</p>"
             + "<hr style=\"border:none;border-top:1px solid #eee;margin:24px 0\">"
             + "<p style=\"color:#888;font-size:12px\">This is an automated message from the SANLY "
             + "e-Government Platform. Do not reply to this email.</p>"
             + "</div></body></html>";
    }
}
