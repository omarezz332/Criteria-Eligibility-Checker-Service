package com.eligibility.infrastructure.email;

import com.eligibility.application.port.out.EmailNotificationPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/**
 * Mock email adapter — logs the email instead of sending it.
 * Replace this class with SmtpEmailAdapter or SesEmailAdapter in production.
 * No application layer changes needed — just swap the @Component implementation.
 */
@Component
public class MockEmailAdapter implements EmailNotificationPort {

    private static final Logger log = LoggerFactory.getLogger(MockEmailAdapter.class);

    @Async
    @Override
    public void sendPreferenceConfirmation(
            UUID applicantId,
            String recipientEmail,
            String applicantName,
            List<String> lotteryNames
    ) {
        log.info("[MOCK EMAIL] To: {} | Applicant: {} ({}) | Lotteries: {}",
                recipientEmail, applicantName, applicantId, lotteryNames);
    }
}
