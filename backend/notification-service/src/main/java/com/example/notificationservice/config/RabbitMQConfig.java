package com.example.notificationservice.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String ORDER_CREATED_QUEUE = "order.created.queue";
    public static final String ORDER_CREATED_EXCHANGE = "order.created.exchange";

    @Bean
    public Queue orderCreatedQueue() {
        return new Queue(ORDER_CREATED_QUEUE, false);
    }

    @Bean
    public TopicExchange orderCreatedExchange() {
        return new TopicExchange(ORDER_CREATED_EXCHANGE);
    }

    @Bean
    public Binding binding(Queue orderCreatedQueue, TopicExchange orderCreatedExchange) {
        return BindingBuilder.bind(orderCreatedQueue).to(orderCreatedExchange).with("order.created.#");
    }
}