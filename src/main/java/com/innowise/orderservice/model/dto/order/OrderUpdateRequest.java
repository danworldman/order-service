package com.innowise.orderservice.model.dto.order;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record OrderUpdateRequest(
        @NotBlank(message = "Status is required and cannot be blank")
        @Pattern(
                regexp = "CREATED|PENDING|CONFIRMED|PAID|CANCELLED|SHIPPED|DELIVERED",
                message = "Invalid order status. Allowed values are: CREATED, PENDING, CONFIRMED, PAID, CANCELLED, SHIPPED, DELIVERED"
        )
        String status
) {}