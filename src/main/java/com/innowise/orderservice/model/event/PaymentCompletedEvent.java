package com.innowise.orderservice.model.event;

public record PaymentCompletedEvent(
        Long orderId,
        String status
) {}