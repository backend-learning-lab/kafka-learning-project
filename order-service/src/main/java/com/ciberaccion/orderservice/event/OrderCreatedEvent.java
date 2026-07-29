package com.ciberaccion.orderservice.event;

import java.math.BigDecimal;
import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** CONTRATO DEL EVENTO - contrato entre servicios -
 * Copia del evento OrderCreatedEvent publicado por order-service.
 * Debe mantenerse estructuralmente idéntica a la del productor.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderCreatedEvent {

    private String orderId;
    private String customerId;
    private String product;
    private Integer quantity;
    private BigDecimal totalAmount;
    private Instant createdAt;
}
