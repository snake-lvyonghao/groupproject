package com.comp5348.Common.config;

import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MessagingConfig {
    public static final String DELIVERY_QUEUE = "delivery.request.queue";
    public static final String DELIVERR_RESPONSE_QUEUE = "delivery.response.queue";
    public static final String EMAIL_QUEUE = "email.request.queue";

    // Bean definition for RabbitMQ queue
    // Creates and registers a RabbitMQ queue bean named "delivery.request.queue"
    @Bean
    public Queue deliveryQueue() {
        return new Queue(DELIVERY_QUEUE, true);
    }

    @Bean
    public Queue emailResponseQueue(){
        return new Queue(DELIVERR_RESPONSE_QUEUE, true);
    }

    @Bean
    public Queue emailQueue() {return new Queue(EMAIL_QUEUE, true); }

}
