package com.ciberaccion.notificationservice.listener;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.ciberaccion.notificationservice.event.OrderCreatedEvent;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class OrderNotificationListener {

    @KafkaListener(topics = "${app.kafka.topic.order-events:order-events}", groupId = "${spring.kafka.consumer.group-id}")
    public void handleOrderCreated(OrderCreatedEvent event) {
        log.info("📩 Notificando al cliente {} — pedido {} confirmado: {} x{} (total: {})",
                event.getCustomerId(),
                event.getOrderId(),
                event.getProduct(),
                event.getQuantity(),
                event.getTotalAmount());

        // Aquí iría el envío real (email, SMS, push, etc.).
        // Por ahora simulamos la notificación con el log de arriba.
    }

}
