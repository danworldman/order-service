package com.innowise.orderservice.service;

import com.innowise.orderservice.model.dto.item.ItemResponse;

/**
 * Service for retrieving item information.
 */
public interface ItemService {

    /**
     * Retrieves an item by its id.
     *
     * @param id the item id
     * @return the item matching the given id
     */
    ItemResponse getItemById(Long id);
}