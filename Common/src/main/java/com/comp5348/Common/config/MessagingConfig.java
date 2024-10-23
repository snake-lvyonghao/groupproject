package com.comp5348.Common.config;

import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MessagingConfig {
    // Naming the RabbitMQ queue as "trade_queue"
    public static final String DELIVERY_QUEUE = "delivery.request.queue";
    public static final String EMAIL_QUEUE = "email.request.queue";

    // Bean definition for RabbitMQ queue
    // Creates and registers a RabbitMQ queue bean named trade_queue
    @Bean
    public Queue delivery_queue() {
        return new Queue(DELIVERY_QUEUE, true);
    }

    @Bean
    public Queue email_queue() {return new Queue(EMAIL_QUEUE, true); }
}
