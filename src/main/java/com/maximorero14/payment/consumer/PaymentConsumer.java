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
    UtilsService utilsService;

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
                    for (ConsumerRecord<String, String> rec :
                         c.poll(Duration.ofMillis(100))) {
                        try {
                            System.out.printf("Mensaje recibido: %s%n", rec.value());
                            c.commitSync();          // confirmamos sólo si fue OK
                        } catch (Exception ex) {
                            System.err.printf("Fallo procesando: %s%n", ex.getMessage());
                            // no commit => se reentregará
                        }
                    }
                }
            } catch (Exception ex) {
                log.error("Error al iniciar el consumidor de Kafka: {} {}", ex.getMessage(), utilsService.getStackTraceAsString(ex), ex);
            }
        }, "kafka-consumer").start();
    }

}