package com.comp5348.Common.config;

import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MessagingConfig {
    // Naming the RabbitMQ queue as "delivery.request.queue"
    public static final String QUEUE = "delivery.request.queue";

    // Bean definition for RabbitMQ queue
    // Creates and registers a RabbitMQ queue bean named "delivery.request.queue"
    @Bean
    public Queue queue() {
        return new Queue(QUEUE, true);
    }
}
