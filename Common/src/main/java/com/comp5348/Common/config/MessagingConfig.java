package com.comp5348.Common.config;

import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MessagingConfig {
    // Naming the RabbitMQ queue as "trade_queue"
    public static final String QUEUE = "delivery.request.queue";

    // Bean definition for RabbitMQ queue
    // Creates and registers a RabbitMQ queue bean named trade_queue
    @Bean
    public Queue queue() {
        return new Queue(QUEUE, true);
    }
}
