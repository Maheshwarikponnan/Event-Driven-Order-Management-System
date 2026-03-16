package com.example.orderservice.service;

import com.example.orderservice.dto.OrderDTO;
import com.example.orderservice.entity.Order;
import com.example.orderservice.messaging.OrderEventPublisher;
import com.example.orderservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderEventPublisher orderEventPublisher;

    public Order createOrder(OrderDTO orderDTO) {
        // Logic to create order
        Order order = new Order();
        order.setStatus("CREATED");
        order.setCreatedAt(LocalDateTime.now());
        // Assume user and items are set
        Order savedOrder = orderRepository.save(order);
        orderEventPublisher.publishOrderCreatedEvent(savedOrder);
        return savedOrder;
    }

    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    public Order getOrderById(Long id) {
        return orderRepository.findById(id).orElse(null);
    }

    public void cancelOrder(Long id) {
        Order order = getOrderById(id);
        if (order != null) {
            order.setStatus("CANCELLED");
            orderRepository.save(order);
        }
    }
}