package com.innowise.orderservice.model.dto.order;

import jakarta.validation.constraints.NotBlank;

public record OrderUpdateRequest(
        @NotBlank(message = "Status is required and cannot be blank")
        String status
) {}