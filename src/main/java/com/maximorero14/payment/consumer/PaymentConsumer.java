package com.maximorero14.payment.consumer;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.maximorero14.payment.dto.PaymentRequest;
import com.maximorero14.payment.service.PaymentService;
import com.maximorero14.payment.service.UtilsService;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class PaymentConsumer {

    @Value("${kafka.bootstrap-servers}")
    private String bootstrap;

    @Value("${kafka.payment.topic}")
    private String topic;

    @Autowired
    private UtilsService utilsService;

    @Autowired
    private PaymentService paymentService;

    private final ObjectMapper objectMapper = new ObjectMapper(); // Para deserializar el JSON

    @PostConstruct
    public void start() {
        new Thread(() -> {
            Properties p = new Properties();
            p.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrap);
            p.put(ConsumerConfig.GROUP_ID_CONFIG, "payment_group");
            p.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
                  "org.apache.kafka.common.serialization.StringDeserializer");
            p.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
                  "org.apache.kafka.common.serialization.StringDeserializer");
            p.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

            try (KafkaConsumer<String, String> c = new KafkaConsumer<>(p)) {
                c.subscribe(Collections.singletonList(topic));

                while (true) {
                    for (ConsumerRecord<String, String> rec : c.poll(Duration.ofMillis(100))) {
                        try {
                            log.info("Mensaje recibido: {}", rec.value());

                            // Convertir el mensaje en un objeto PaymentRequest
                            PaymentRequest paymentRequest = objectMapper.readValue(rec.value(), PaymentRequest.class);

                            // Llamar al servicio de pago
                            paymentService.processPayment(paymentRequest);

                            // Confirmar el mensaje solo si fue procesado correctamente
                            c.commitSync();
                        } catch (Exception ex) {
                            log.error("Fallo procesando el mensaje: {}", ex.getMessage(), ex);
                            // No se confirma el commit para que el mensaje se reentregue
                        }
                    }
                }
            } catch (Exception ex) {
                log.error("Error al iniciar el consumidor de Kafka: {} {}", ex.getMessage(), utilsService.getStackTraceAsString(ex), ex);
            }
        }, "kafka-consumer").start();
    }
}