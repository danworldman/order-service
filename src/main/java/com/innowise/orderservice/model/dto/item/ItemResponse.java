package com.innowise.orderservice.model.dto.item;

import java.math.BigDecimal;

public record ItemResponse(
        Long id,
        String name,
        BigDecimal price
) {}