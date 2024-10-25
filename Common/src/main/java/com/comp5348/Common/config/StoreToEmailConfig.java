package com.comp5348.Common.config;

import org.springframework.amqp.core.Queue;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(name = "spring.rabbitmq.enable", havingValue = "true")
public class StoreToEmailConfig {
    public static final String QUEUE = "store.to.email.queue";

    @Bean
    public Queue storeToEmailQueue() {
        System.out.println("Registering deliveryResponseQueue bean");
        return new Queue(QUEUE, true);

    }
}
