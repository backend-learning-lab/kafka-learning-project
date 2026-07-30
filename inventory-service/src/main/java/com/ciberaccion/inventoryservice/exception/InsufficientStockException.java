package com.ciberaccion.inventoryservice.exception;

/**
 * Se lanza cuando se intenta descontar más stock del que hay disponible.
 * Al propagarse desde el @KafkaListener, activa el mecanismo de retry + Dead Letter Topic
 * configurado en KafkaErrorHandlingConfig.
 */
public class InsufficientStockException extends RuntimeException{

    public InsufficientStockException(String product, int available, int requested) {
        super("Stock insuficiente para '%s': disponible=%d, solicitado=%d"
                .formatted(product, available, requested));
    }    
}
