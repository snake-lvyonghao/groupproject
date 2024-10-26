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
    public Queue emailResponseQueue(){
        return new Queue(DELIVERR_RESPONSE_QUEUE, true);
    }

    @Bean
    public Queue emailQueue() {return new Queue(EMAIL_QUEUE, true); }


    @Bean
    public Queue Queue() {return new Queue(PACK_QUEUE, true); }

    @Bean
    public Queue delayQueue() {
        // 配置延迟队列的参数
        Map<String, Object> args = new HashMap<>();

        // 设置死信交换器，用于将消息从延迟队列转发到实际处理队列中
        args.put("x-dead-letter-exchange", "direct-exchange");
        // 设置死信路由键，用于将消息从延迟队列转发到指定的路由键对应的队列
        args.put("x-dead-letter-routing-key", PACK_QUEUE);
        // 设置消息的 TTL（存活时间），10秒
        args.put("x-message-ttl", 10000);

        // 创建一个名为 "delay.delivery.request.queue" 的持久化队列
        return new Queue(DELAY_QUEUE, true, false, false, args);
    }

    // 配置交换器
    @Bean
    public DirectExchange directExchange() {
        return new DirectExchange("direct-exchange");
    }

    // 绑定实际处理队列到交换器
    @Bean
    public Binding bindDeliveryQueue(Queue deliveryQueue, DirectExchange directExchange) {
        return BindingBuilder.bind(deliveryQueue).to(directExchange).with(PACK_QUEUE);
    }

}
