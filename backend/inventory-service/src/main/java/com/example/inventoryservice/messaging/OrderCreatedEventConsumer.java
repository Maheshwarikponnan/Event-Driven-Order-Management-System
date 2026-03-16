package com.example.inventoryservice.messaging;

import com.example.inventoryservice.entity.Order;
import com.example.inventoryservice.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderCreatedEventConsumer {

    private final InventoryService inventoryService;

    @RabbitListener(queues = RabbitMQConfig.ORDER_CREATED_QUEUE)
    public void handleOrderCreated(Order order) {
        // Assume order has orderItems
        order.getOrderItems().forEach(item -> {
            inventoryService.updateStock(item.getProduct().getId(), item.getQuantity());
        });
    }
}