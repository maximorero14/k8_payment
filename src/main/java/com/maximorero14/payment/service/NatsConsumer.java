package com.maximorero14.payment.service;

import java.nio.charset.StandardCharsets;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.maximorero14.payment.dto.PaymentRequest;

import io.nats.client.Connection;
import io.nats.client.Dispatcher;
import io.nats.client.Nats;
import io.nats.client.Options;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class NatsConsumer {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${nats.url}")
    private String natsUrl;

    @Value("${nats.subject}")
    private String subject;

    @Autowired
    UtilsService utilsService;

    private Connection natsConnection;

    @PostConstruct
    public void subscribe() {
        log.info("Subscribing to NATS subject: {} {}", natsUrl, subject);
        try {
            natsConnection = Nats.connect(new Options.Builder().server(natsUrl).build());
            Dispatcher dispatcher = natsConnection.createDispatcher((msg) -> {
                try {
                    String json = new String(msg.getData(), StandardCharsets.UTF_8);
                    PaymentRequest paymentRequest = objectMapper.readValue(json, PaymentRequest.class);
                    handlePaymentRequest(paymentRequest);
                } catch (Exception e) {
                    log.error("Error processing message from NATS: {} {}", e.getMessage(), utilsService.getStackTraceAsString(e), e);
                }
            });
            dispatcher.subscribe(subject);

            log.info("Successfully subscribed to NATS subject: {}", subject);
        } catch (Exception e) {
            log.error("Error subscribing to NATS: {} {}", e.getMessage(), utilsService.getStackTraceAsString(e), e);
            throw new IllegalStateException("Error subscribing to NATS", e);
        }
    }

    @PreDestroy
    public void cleanup() {
        if (natsConnection != null) {
            try {
                natsConnection.close();
                log.info("NATS connection closed successfully");
            } catch (Exception e) {
                log.error("Error closing NATS connection: {}", e.getMessage(), e);
            }
        }
    }

    private void handlePaymentRequest(PaymentRequest paymentRequest) {
        // Process the received PaymentRequest
        log.info("Received PaymentRequest: " + paymentRequest);
    }
}
