package com.ecommers.orders.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class OrderDto {

    public record OrderRequest(
            @NotNull(message = "userId es obligatorio")
            Long userId,
            @NotNull(message = "productId es obligatorio")
            Long productId,
            @NotNull(message = "quantity es obligatorio") @Min(value = 1, message = "La cantidad debe ser al menos 1")
            Integer quantity
    ) {}

    public record OrderResponse(
            Long id,
            Long userId,
            Long productId,
            Integer quantity,
            Double totalAmount,
            String status) {}

    public record ProductDto(Long id, String name, String description, Double price) {}

    public record PaymentDto(String id, String status) {}
}
