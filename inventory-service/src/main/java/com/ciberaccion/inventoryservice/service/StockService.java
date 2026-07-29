package com.ciberaccion.inventoryservice.service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

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
    }

    public void decreaseStock(String product, int quantity) {
        int currentStock = stockByProduct.getOrDefault(product, 0);

        if (currentStock < quantity) {
            log.warn("⚠️ Stock insuficiente para '{}': disponible={}, solicitado={}",
                    product, currentStock, quantity);
            stockByProduct.put(product, 0);
            return;
        }

        int newStock = currentStock - quantity;
        stockByProduct.put(product, newStock);
        log.info("📦 Stock actualizado — '{}': {} -> {}", product, currentStock, newStock);
    }

    public int getStock(String product) {
        return stockByProduct.getOrDefault(product, 0);
    }

}
