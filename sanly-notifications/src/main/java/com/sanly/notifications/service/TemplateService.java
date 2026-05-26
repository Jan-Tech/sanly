package com.sanly.notifications.service;

import com.sanly.notifications.entity.Channel;
import com.sanly.notifications.entity.EventType;
import com.sanly.notifications.entity.Language;
import com.sanly.notifications.entity.NotificationTemplate;
import com.sanly.notifications.exception.TemplateNotFoundException;
import com.sanly.notifications.repository.NotificationTemplateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class TemplateService {

    private final NotificationTemplateRepository templateRepository;

    private static final Pattern HTML_TAG = Pattern.compile("<[^>]*>");

    public NotificationTemplate resolve(EventType eventType, Language language) {
        // Try ALL channel first (most templates use ALL), then any active template
        return templateRepository
                .findByEventTypeAndLanguageAndChannelAndIsActiveTrue(eventType, language, Channel.ALL)
                .or(() -> templateRepository.findByEventTypeAndLanguageAndIsActiveTrue(eventType, language))
                .orElseThrow(() -> new TemplateNotFoundException(eventType.name(), language.name()));
    }

    public String fill(String template, Map<String, String> variables) {
        if (variables == null || variables.isEmpty()) return template;
        String result = template;
        for (Map.Entry<String, String> entry : variables.entrySet()) {
            String safeValue = stripHtml(entry.getValue());
            result = result.replace("{" + entry.getKey() + "}", safeValue);
        }
        return result;
    }

    /** Strip HTML tags from metadata values to prevent injection into notification bodies. */
    private String stripHtml(String value) {
        if (value == null) return "";
        return HTML_TAG.matcher(value).replaceAll("");
    }
}
