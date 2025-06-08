package com.maximorero14.payment.service;

import java.nio.charset.StandardCharsets;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.maximorero14.payment.dto.PaymentRequest;

import io.nats.client.Connection;
import io.nats.client.Dispatcher;
import io.nats.client.Nats;
import io.nats.client.Options;
import jakarta.annotation.PostConstruct;
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

    @PostConstruct
    public void subscribe() {
        try (Connection natsConnection = Nats.connect(new Options.Builder().server(natsUrl).build())) {
            Dispatcher dispatcher = natsConnection.createDispatcher((msg) -> {
                try {
                    String json = new String(msg.getData(), StandardCharsets.UTF_8);
                    PaymentRequest paymentRequest = objectMapper.readValue(json, PaymentRequest.class);
                    handlePaymentRequest(paymentRequest);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            dispatcher.subscribe(subject);
        } catch (Exception e) {
            log.error("Error subscribing to NATS: " + e.getMessage());
            //throw new IllegalStateException("Error subscribing to NATS", e);
        }
    }

    private void handlePaymentRequest(PaymentRequest paymentRequest) {
        // Process the received PaymentRequest
        log.info("Received PaymentRequest: " + paymentRequest);
    }
}
