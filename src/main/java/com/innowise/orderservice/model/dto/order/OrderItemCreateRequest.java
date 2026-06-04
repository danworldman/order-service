package com.innowise.orderservice.model.dto.order;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record OrderItemCreateRequest(
        @NotNull(message = "Item ID is required")
        Long itemId,

        @NotNull(message = "Quantity is required and cannot be blank")
        @Positive(message = "Quantity must be greater than zero")
        Long quantity
) {}