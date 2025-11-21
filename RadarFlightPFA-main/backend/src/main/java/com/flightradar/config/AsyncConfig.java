package com.flightradar.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Configuration pour activer les méthodes asynchrones (@Async)
 */
@Configuration
@EnableAsync
public class AsyncConfig {
    // Configuration par défaut - utilise le pool de threads Spring
}

