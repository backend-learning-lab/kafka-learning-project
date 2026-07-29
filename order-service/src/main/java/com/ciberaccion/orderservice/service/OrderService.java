package com.ciberaccion.orderservice.service;

import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.ciberaccion.orderservice.event.OrderCreatedEvent;
import com.ciberaccion.orderservice.mapper.OrderEventMapper;
import com.ciberaccion.orderservice.model.Order;
import com.ciberaccion.orderservice.repository.OrderRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderEventMapper orderEventMapper;
    private final KafkaTemplate<String, OrderCreatedEvent> kafkaTemplate;
    
    @Value("${app.kafka.topic.order-events:order-events}")
    private String orderEventsTopic;

    public Order createOrder(String customerId, String product, Integer quantity, BigDecimal totalAmount) {
        Order order = Order.builder()
                .customerId(customerId)
                .product(product)
                .quantity(quantity)
                .totalAmount(totalAmount)
                .build();
 
        Order savedOrder = orderRepository.save(order);
        log.info("Order persisted with id={}", savedOrder.getId());
 
        publishOrderCreatedEvent(savedOrder);
 
        return savedOrder;
    }

    private void publishOrderCreatedEvent(Order order) {
        OrderCreatedEvent event = orderEventMapper.toEvent(order);
 
        // Usamos el orderId como key para que Kafka mantenga el orden
        // de los eventos de un mismo pedido dentro de la misma partición.
        kafkaTemplate.send(orderEventsTopic, event.getOrderId(), event);
 
        log.info("OrderCreatedEvent published for orderId={} on topic={}",
                event.getOrderId(), orderEventsTopic);
    }

}
