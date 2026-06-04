package com.innowise.orderservice.model.dto.order;

import com.innowise.orderservice.model.dto.item.ItemResponse;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record OrderItemResponse(
        @NotNull
        Long id,

        @NotNull
        ItemResponse item,

        @NotNull(message = "Quantity is required and cannot be blank")
        @Positive(message = "Quantity must be positive")
        Long quantity
) {}