package com.comp5348.Common.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class MessagingConfig {
    public static final String DELIVERY_QUEUE = "delivery.request.queue";
    public static final String DELIVERR_RESPONSE_QUEUE = "delivery.response.queue";
    public static final String EMAIL_QUEUE = "email.request.queue";
    public static final String DELAY_QUEUE = "delay.delivery.request.queue";
    public static final String PACK_QUEUE = "pack.delivery.request.queue";

    // Bean definition for RabbitMQ queue
    // Creates and registers a RabbitMQ queue bean named "delivery.request.queue"
    @Bean
    public Queue deliveryQueue() {
        return new Queue(DELIVERY_QUEUE, true);
    }

    @Bean
    public Queue deliverrResponseQueue() {
        return new Queue(DELIVERR_RESPONSE_QUEUE, true);
    }

    @Bean
    public Queue emailQueue() { return new Queue(EMAIL_QUEUE, true); }

    @Bean
    public Queue packQueue() { return new Queue(PACK_QUEUE, true); }

    @Bean
    public Queue delayQueue() {
        // args
        Map<String, Object> args = new HashMap<>();

        args.put("x-dead-letter-exchange", "direct-exchange");
        // The dead-letter routing key is used to forward messages from the delay queue to the queue corresponding to the specified routing key
        args.put("x-dead-letter-routing-key", PACK_QUEUE);
        // Set the TTL (live time) of the message to 10 seconds
        args.put("x-message-ttl", 10000);


        return new Queue(DELAY_QUEUE, true, false, false, args);
    }

    @Bean
    public DirectExchange directExchange() {
        return new DirectExchange("direct-exchange");
    }

    // bind queue
    @Bean
    public Binding bindPackQueue(Queue packQueue, DirectExchange directExchange) {
        return BindingBuilder.bind(packQueue).to(directExchange).with(PACK_QUEUE);
    }
}
