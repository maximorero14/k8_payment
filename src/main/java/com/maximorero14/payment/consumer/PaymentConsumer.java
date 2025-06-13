package com.maximorero14.payment.consumer;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import com.maximorero14.payment.dto.PaymentRequest;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class PaymentConsumer {

    @RabbitListener(queues = "${payment.queue.name}")
    public void processPaymentRequest(PaymentRequest paymentRequest) {
        try {
            log.info("Received payment request: {}", paymentRequest);
            
            // Aquí procesas la lógica de negocio del pago
            processPayment(paymentRequest);
            
            log.info("Payment request processed successfully for user: {}", 
                    paymentRequest.getUserId());
        } catch (Exception e) {
            log.error("Error processing payment request: {}", e.getMessage(), e);
            // Aquí podrías implementar retry logic o dead letter queue
            throw e; // Re-throw para activar retry automático de RabbitMQ
        }
    }

    private void processPayment(PaymentRequest paymentRequest) {
        // Simular procesamiento del pago
        log.info("Processing payment of {} {} for user {} using method {} {}", 
                paymentRequest.getAmount(), 
                paymentRequest.getCurrency(),
                paymentRequest.getUserId(),
                paymentRequest.getMethod(),
                paymentRequest);
        
        // Aquí iría tu lógica de negocio real:
        // - Validaciones
        // - Llamadas a APIs de pago
        // - Guardado en base de datos
        // - etc.
        
        try {
            Thread.sleep(200); // Simular tiempo de procesamiento
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}