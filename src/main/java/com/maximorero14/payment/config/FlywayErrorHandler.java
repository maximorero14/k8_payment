package com.maximorero14.payment.config;

import org.flywaydb.core.Flyway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;

@Configuration
public class FlywayErrorHandler {

    private static final Logger log = LoggerFactory.getLogger(FlywayErrorHandler.class);

    private final Flyway flyway;

    public FlywayErrorHandler(Flyway flyway) {
        this.flyway = flyway;
    }

    @PostConstruct
    public void migrate() {
        try {
            log.info("Starting Flyway migration...");
            flyway.migrate();
            log.info("Flyway migration completed successfully.");
        } catch (Exception e) {
            log.error("Flyway migration failed: {}", e.getMessage(), e);
            // No lanzar excepción para evitar que la aplicación se detenga
        }
    }
}