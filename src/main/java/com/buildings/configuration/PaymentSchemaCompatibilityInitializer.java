package com.buildings.configuration;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentSchemaCompatibilityInitializer implements ApplicationRunner {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(ApplicationArguments args) {
        try {
            jdbcTemplate.execute("ALTER TABLE payment_transactions MODIFY COLUMN status VARCHAR(20) NOT NULL");
            log.info("Ensured payment_transactions.status is VARCHAR(20)");
        } catch (Exception ex) {
            log.warn("Skip payment schema compatibility fix: {}", ex.getMessage());
        }
    }
}
