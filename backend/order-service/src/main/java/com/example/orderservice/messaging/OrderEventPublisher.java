package com.example.orderservice.messaging;

import com.example.orderservice.entity.Order;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public void publishOrderCreatedEvent(Order order) {
        rabbitTemplate.convertAndSend(RabbitMQConfig.ORDER_CREATED_EXCHANGE, "order.created", order);
    }
}