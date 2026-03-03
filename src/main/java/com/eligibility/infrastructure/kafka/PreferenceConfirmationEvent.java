package com.eligibility.infrastructure.kafka;

import java.util.List;
import java.util.UUID;

public record PreferenceConfirmationEvent(
        UUID applicantId,
        String recipientEmail,
        String applicantName,
        List<String> lotteryNames
) {}
