package com.innowise.orderservice.service.impl;

import com.innowise.orderservice.dao.ItemDAO;
import com.innowise.orderservice.exception.ItemNotFoundException;
import com.innowise.orderservice.mapper.ItemMapper;
import com.innowise.orderservice.model.dto.item.ItemResponse;
import com.innowise.orderservice.model.entity.Item;
import com.innowise.orderservice.service.ItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {

    private final ItemDAO itemDAO;
    private final ItemMapper itemMapper;

    @Override
    @Transactional(readOnly = true)
    public ItemResponse getItemById(Long id) {
        validateId(id);

        Item item = itemDAO.findById(id)
                .orElseThrow(() -> new ItemNotFoundException("Item not found with id: " + id));

        return itemMapper.toDto(item);
    }

    private void validateId(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Item's id cannot be null");
        }

        if (id <= 0) {
            throw new IllegalArgumentException("Item id must be positive");
        }
    }
}