package com.sanly.notifications.service;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class SmsDeliveryService {

    @Value("${twilio.account-sid:}") private String accountSid;
    @Value("${twilio.auth-token:}")  private String authToken;
    @Value("${twilio.from-number:}") private String fromNumber;

    private boolean twilioEnabled;

    @PostConstruct
    void init() {
        twilioEnabled = accountSid != null && !accountSid.isBlank()
                     && authToken  != null && !authToken.isBlank()
                     && fromNumber != null && !fromNumber.isBlank();
        if (twilioEnabled) {
            Twilio.init(accountSid, authToken);
            log.info("Twilio SMS delivery enabled (from={})", fromNumber);
        } else {
            log.info("Twilio credentials not set — SMS delivery in STUB mode");
        }
    }

    public void sendSms(String toNumber, String messageBody) {
        if (!twilioEnabled) {
            log.info("[SMS-STUB] To: {} | Body: {}", toNumber, messageBody);
            return;
        }
        try {
            Message msg = Message.creator(
                    new PhoneNumber(toNumber),
                    new PhoneNumber(fromNumber),
                    messageBody
            ).create();
            log.info("[SMS] Sent SID={} to={}", msg.getSid(), toNumber);
        } catch (Exception ex) {
            log.error("[SMS] Failed to send to={}: {}", toNumber, ex.getMessage());
        }
    }
}
