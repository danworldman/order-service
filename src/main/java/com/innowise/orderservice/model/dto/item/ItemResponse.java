package com.innowise.orderservice.model.dto.item;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record ItemResponse(
        @NotNull(message = "Item ID is required")
        Long id,

        @NotBlank(message = "Item name is required and cannot be blank")
        String name,

        @NotNull(message = "Price is required and cannot be blank")
        @Positive(message = "Price must be positive")
        BigDecimal price
) {}