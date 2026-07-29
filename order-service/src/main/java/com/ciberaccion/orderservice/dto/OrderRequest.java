package com.ciberaccion.orderservice.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record OrderRequest(

        @NotBlank(message = "customerId is required")
        String customerId,

        @NotBlank(message = "product is required")
        String product,

        @NotNull(message = "quantity is required")
        @Positive(message = "quantity must be greater than 0")
        Integer quantity,

        @NotNull(message = "totalAmount is required")
        @DecimalMin(value = "0.0", inclusive = false, message = "totalAmount must be greater than 0")
        BigDecimal totalAmount
) {
}