package com.maximorero14.payment.config;


import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Value("${payment.queue.name}")
    private String queueName;

    @Value("${payment.exchange.name}")
    private String exchangeName;

    @Value("${payment.routing.key}")
    private String routingKey;

    @Bean
    public Queue paymentQueue() {
        return new Queue(queueName, true); // durable = true
    }

    @Bean
    public DirectExchange paymentExchange() {
        return new DirectExchange(exchangeName);
    }

    @Bean
    public Binding binding() {
        return BindingBuilder
                .bind(paymentQueue())
                .to(paymentExchange())
                .with(routingKey);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }
}