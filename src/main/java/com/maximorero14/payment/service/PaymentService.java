package com.maximorero14.payment.service;

import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.maximorero14.payment.dto.PaymentRequest;

import io.opentelemetry.instrumentation.annotations.WithSpan;


@Service
public class PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);

    private final Random random = new Random();

    @WithSpan
    public void savePayment(PaymentRequest paymentRequest) {
        try {
            // Simula un retraso aleatorio entre 500ms y 1000ms
            int delay = 500 + random.nextInt(501); // 500ms a 1000ms
            Thread.sleep(delay);

            // Simula el guardado en la base de datos
            log.info("Payment saved: {}", paymentRequest);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Error during save simulation: {}", e.getMessage());
        }
    }
}