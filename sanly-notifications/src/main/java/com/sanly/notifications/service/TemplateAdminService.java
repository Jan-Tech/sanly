package com.sanly.notifications.service;

import com.sanly.notifications.dto.request.UpdateTemplateRequest;
import com.sanly.notifications.dto.response.TemplateResponse;
import com.sanly.notifications.entity.NotificationTemplate;
import com.sanly.notifications.repository.NotificationTemplateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TemplateAdminService {

    private final NotificationTemplateRepository templateRepository;

    @Transactional(readOnly = true)
    public List<TemplateResponse> listAll() {
        return templateRepository.findAllByIsActiveTrueOrderByEventTypeAscLanguageAsc()
                .stream().map(this::toResponse).toList();
    }

    @Transactional
    public TemplateResponse update(UUID templateId, UpdateTemplateRequest req) {
        NotificationTemplate template = templateRepository.findById(templateId)
                .orElseThrow(() -> new RuntimeException("Template not found: " + templateId));
        template.setTitleTemplate(req.getTitleTemplate());
        template.setBodyTemplate(req.getBodyTemplate());
        if (req.getIsActive() != null) template.setActive(req.getIsActive());
        return toResponse(templateRepository.save(template));
    }

    private TemplateResponse toResponse(NotificationTemplate t) {
        return TemplateResponse.builder()
                .templateId(t.getTemplateId())
                .eventType(t.getEventType())
                .titleTemplate(t.getTitleTemplate())
                .bodyTemplate(t.getBodyTemplate())
                .channel(t.getChannel())
                .language(t.getLanguage())
                .isActive(t.isActive())
                .build();
    }
}
