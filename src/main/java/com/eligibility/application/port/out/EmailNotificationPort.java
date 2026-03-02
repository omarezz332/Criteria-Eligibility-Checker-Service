package com.eligibility.application.port.out;

import com.eligibility.domain.model.Lottery;

import java.util.List;
import java.util.UUID;

/**
 * Outbound port: Email notification contract.
 * <p>
 * Implemented by: MockEmailAdapter (infrastructure layer)
 * Later replaced by: SmtpEmailAdapter or SesEmailAdapter with no application layer changes.
 * <p>
 * This method is called AFTER a successful preference submission.
 * The implementation runs asynchronously (@Async) — it must never
 * block the main transaction or the HTTP response.
 * <p>
 * The application layer calls this interface and knows nothing about
 * SMTP, SES, Kafka, or any actual email mechanism.
 */
public interface EmailNotificationPort {

    /**
     * Send a submission confirmation email to the applicant.
     *
     * @param applicantId    for logging/tracing
     * @param recipientEmail the applicant's email address
     * @param applicantName  used in the email body greeting
     * @param lotteryNames   the list of lottery names in their preference list
     */
    void sendPreferenceConfirmation(
            UUID applicantId,
            String recipientEmail,
            String applicantName,
            List<Lottery> lotteryNames
    );
}