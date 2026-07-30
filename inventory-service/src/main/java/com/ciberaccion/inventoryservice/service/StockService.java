package com.ciberaccion.inventoryservice.service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import com.ciberaccion.inventoryservice.exception.InsufficientStockException;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class StockService {

    private final Map<String, Integer> stockByProduct = new ConcurrentHashMap<>();

    public StockService() {
        // Stock inicial de ejemplo
        stockByProduct.put("Teclado mecanico", 50);
        stockByProduct.put("Mouse inalambrico", 100);
        stockByProduct.put("Monitor 24 pulgadas", 20);

        // Producto sin stock a propósito: útil para probar retry + Dead Letter Topic
        // con un pedido real que legítimamente no se puede cumplir.
        stockByProduct.put("Silla Gamer", 0);        
    }

    public void decreaseStock(String product, int quantity) {
        int currentStock = stockByProduct.getOrDefault(product, 0);
 
        if (currentStock < quantity) {
            log.warn("⚠️ Stock insuficiente para '{}': disponible={}, solicitado={}",
                    product, currentStock, quantity);
            throw new InsufficientStockException(product, currentStock, quantity);
        }
 
        int newStock = currentStock - quantity;
        stockByProduct.put(product, newStock);
        log.info("📦 Stock actualizado — '{}': {} -> {}", product, currentStock, newStock);
    }

    public int getStock(String product) {
        return stockByProduct.getOrDefault(product, 0);
    }

}
