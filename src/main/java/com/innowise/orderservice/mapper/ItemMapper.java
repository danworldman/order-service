package com.innowise.orderservice.mapper;

import com.innowise.orderservice.model.dto.item.ItemResponse;
import com.innowise.orderservice.model.entity.Item;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ItemMapper {
    ItemResponse toDto(Item item);
}