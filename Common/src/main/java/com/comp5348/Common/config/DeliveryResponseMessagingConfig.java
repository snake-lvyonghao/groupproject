package com.comp5348.Common.config;

import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DeliveryResponseMessagingConfig {
    //这是快递公司返回信息的queue.
    public static final String QUEUE = "delivery.response.queue";

    // Bean definition for RabbitMQ queue
    // Creates and registers a RabbitMQ queue bean named "delivery.response.queue"
    @Bean
    public Queue queue() {
        return new Queue(QUEUE, true);
    }
}
