package com.innowise.orderservice.mapper;

import com.innowise.orderservice.model.dto.order.OrderItemCreateRequest;
import com.innowise.orderservice.model.dto.order.OrderItemResponse;
import com.innowise.orderservice.model.dto.order.OrderResponse;
import com.innowise.orderservice.model.dto.order.OrderUpdateRequest;
import com.innowise.orderservice.model.dto.user.UserResponse;
import com.innowise.orderservice.model.entity.Order;
import com.innowise.orderservice.model.entity.OrderItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        uses = {ItemMapper.class})
public interface OrderMapper {

    @Mapping(target = "id", source = "order.id")
    @Mapping(target = "user", source = "user")
    @Mapping(target = "items", source = "order.orderItems")
    OrderResponse toOrderDto(Order order, UserResponse user);

    @Mapping(source = "item", target = "item")
    OrderItemResponse toOrderItemDto(OrderItem orderItem);

    @Mapping(target = "item.id", source = "itemId")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "order", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    OrderItem toOrderItemEntity(OrderItemCreateRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "totalPrice", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "orderItems", ignore = true)
    void updateOrderEntityStatus(@MappingTarget Order order, OrderUpdateRequest request);
}