package com.ciberaccion.orderservice.mapper;

import com.ciberaccion.orderservice.event.OrderCreatedEvent;
import com.ciberaccion.orderservice.model.Order;
import org.springframework.stereotype.Component;

@Component
public class OrderEventMapper {

    public OrderCreatedEvent toEvent(Order order) {
        return OrderCreatedEvent.builder()
                .orderId(order.getId().toString())
                .customerId(order.getCustomerId())
                .product(order.getProduct())
                .quantity(order.getQuantity())
                .totalAmount(order.getTotalAmount())
                .createdAt(order.getCreatedAt())
                .build();
    }
}