package com.ciberaccion.inventoryservice.listener;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.ciberaccion.inventoryservice.event.OrderCreatedEvent;
import com.ciberaccion.inventoryservice.service.StockService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@RequiredArgsConstructor
public class OrderInventoryListener {

    private final StockService stockService;

    @KafkaListener(topics = "${app.kafka.topic.order-events:order-events}", groupId = "${spring.kafka.consumer.group-id}")
    public void handleOrderCreated(OrderCreatedEvent event) {
        log.info("Procesando descuento de inventario para orderId={}", event.getOrderId());
        stockService.decreaseStock(event.getProduct(), event.getQuantity());
    }

}
