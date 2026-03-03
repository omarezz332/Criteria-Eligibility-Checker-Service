package com.eligibility.infrastructure.kafka;

import com.eligibility.application.port.out.EmailNotificationPort;
import com.eligibility.domain.model.Lottery;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class KafkaEmailAdapter implements EmailNotificationPort {

    private static final Logger log = LoggerFactory.getLogger(KafkaEmailAdapter.class);
    private static final String TOPIC = "preference.confirmation";

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public KafkaEmailAdapter(KafkaTemplate<String, String> kafkaTemplate, ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public void sendPreferenceConfirmation(
            UUID applicantId,
            String recipientEmail,
            String applicantName,
            List<Lottery> lotteries
    ) {
        List<String> lotteryNames = lotteries.stream()
                .map(Lottery::name)
                .toList();

        PreferenceConfirmationEvent event = new PreferenceConfirmationEvent(
                applicantId,
                recipientEmail,
                applicantName,
                lotteryNames
        );

        try {
            String payload = objectMapper.writeValueAsString(event);
            log.info("Sending preference confirmation event to Kafka for applicant: {}", applicantId);
            kafkaTemplate.send(TOPIC, applicantId.toString(), payload);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize PreferenceConfirmationEvent for applicant: {}", applicantId, e);
        }
    }
}
