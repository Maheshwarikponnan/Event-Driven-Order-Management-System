package com.example.notificationservice.messaging;

import com.example.notificationservice.entity.Order;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class OrderCreatedEventConsumer {

    @RabbitListener(queues = RabbitMQConfig.ORDER_CREATED_QUEUE)
    public void handleOrderCreated(Order order) {
        // Simulate sending notification
        log.info("Sending order confirmation notification for order {}", order.getId());
        // In real app, send email or SMS
    }
}