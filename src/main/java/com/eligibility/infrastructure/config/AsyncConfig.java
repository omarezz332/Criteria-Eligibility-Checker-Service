package com.eligibility.infrastructure.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Enables Spring's @Async support.
 * Used by MockEmailAdapter to send email notifications
 * without blocking the main transaction or HTTP response.
 */
@Configuration
@EnableAsync
public class AsyncConfig {
}
