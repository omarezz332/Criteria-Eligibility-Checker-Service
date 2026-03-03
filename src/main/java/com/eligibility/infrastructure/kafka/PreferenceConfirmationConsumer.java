package com.eligibility.infrastructure.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class PreferenceConfirmationConsumer {

    private static final Logger log = LoggerFactory.getLogger(PreferenceConfirmationConsumer.class);

    private final ObjectMapper objectMapper;

    public PreferenceConfirmationConsumer(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "preference.confirmation", groupId = "email-service")
    public void consume(String message) {
        try {
            PreferenceConfirmationEvent event = objectMapper.readValue(message, PreferenceConfirmationEvent.class);
            log.info("[EMAIL] To: {} | Applicant: {} | Lotteries: {}",
                    event.recipientEmail(), event.applicantName(), event.lotteryNames());
        } catch (JsonProcessingException e) {
            log.error("Failed to deserialize PreferenceConfirmationEvent: {}", message, e);
        }
    }
}
