package com.innowise.orderservice.model.dto.order;

import com.innowise.orderservice.model.dto.item.ItemResponse;

public record OrderItemResponse(
        Long id,
        ItemResponse item,
        Long quantity
) {}