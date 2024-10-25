package com.comp5348.Common.config;

import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MessagingConfig {
    public static final String QUEUE = "delivery.request.queue";

    @Bean
    public Queue deliveryRequestQueue() {
        return new Queue(QUEUE, true);
    }
}
