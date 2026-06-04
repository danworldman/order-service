package com.innowise.orderservice.service;

import com.innowise.orderservice.model.dto.item.ItemResponse;

public interface ItemService {
    ItemResponse getItemById(Long id);
}