package com.innowise.orderservice.model.event;

import java.math.BigDecimal;

public record OrderCreatedEvent(
        Long orderId,
        Long userId,
        BigDecimal totalPrice
) {}