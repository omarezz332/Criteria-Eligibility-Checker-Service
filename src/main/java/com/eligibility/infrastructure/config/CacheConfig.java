package com.eligibility.infrastructure.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Configuration;

/**
 * Enables Spring's annotation-driven cache management.
 * Used by LotteryRepositoryAdapter to cache active lotteries with their criteria.
 * Cache name: "active-lotteries"
 */
@Configuration
@EnableCaching
public class CacheConfig {
}
