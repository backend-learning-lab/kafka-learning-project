package com.ciberaccion.orderservice.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.ciberaccion.orderservice.dto.OrderRequest;
import com.ciberaccion.orderservice.model.Order;
import com.ciberaccion.orderservice.repository.OrderRepository;
import com.ciberaccion.orderservice.service.OrderService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final OrderRepository orderRepository;
    
    @PostMapping
    public ResponseEntity<Order> createOrder(@Valid @RequestBody OrderRequest request) {
        Order order = orderService.createOrder(
                request.customerId(),
                request.product(),
                request.quantity(),
                request.totalAmount()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(order);
    }

    @GetMapping
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }
 
    @GetMapping("/{id}")
    public Order getOrderById(@PathVariable UUID id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found: " + id));
    }    

}
