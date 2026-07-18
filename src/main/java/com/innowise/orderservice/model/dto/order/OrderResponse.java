package com.innowise.orderservice.model.dto.order;

import com.innowise.orderservice.model.dto.user.UserResponse;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderResponse(
        Long id,
        UserResponse user,
        String status,
        BigDecimal totalPrice,
        List<OrderItemResponse> items,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}