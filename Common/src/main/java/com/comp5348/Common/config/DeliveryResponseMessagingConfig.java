package com.comp5348.Common.config;

import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DeliveryResponseMessagingConfig {
    public static final String QUEUE = "delivery.response.queue";

    // Bean definition for RabbitMQ queue
    // Creates and registers a RabbitMQ queue bean named trade_queue
    @Bean
    public Queue deliveryResponseQueue() {
        return new Queue(QUEUE, true);
    }
}
