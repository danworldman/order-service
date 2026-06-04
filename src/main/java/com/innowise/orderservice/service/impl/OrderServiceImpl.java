package com.innowise.orderservice.service.impl;

import com.innowise.orderservice.client.UserServiceClient;
import com.innowise.orderservice.dao.OrderDAO;
import com.innowise.orderservice.dao.specification.OrderSpecification;
import com.innowise.orderservice.exception.OrderAlreadyDeletedException;
import com.innowise.orderservice.exception.OrderNotFoundException;
import com.innowise.orderservice.exception.UserNotFoundException;
import com.innowise.orderservice.mapper.OrderMapper;
import com.innowise.orderservice.model.dto.item.ItemResponse;
import com.innowise.orderservice.model.dto.order.OrderCreateRequest;
import com.innowise.orderservice.model.dto.order.OrderResponse;
import com.innowise.orderservice.model.dto.order.OrderUpdateRequest;
import com.innowise.orderservice.model.dto.user.UserResponse;
import com.innowise.orderservice.model.entity.Item;
import com.innowise.orderservice.model.entity.Order;
import com.innowise.orderservice.model.entity.OrderItem;
import com.innowise.orderservice.service.ItemService;
import com.innowise.orderservice.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderDAO orderDAO;
    private final OrderMapper orderMapper;
    private final UserServiceClient userServiceClient;
    private final ItemService itemService;

    @Override
    @Transactional
    public OrderResponse createOrder(OrderCreateRequest request) {
        UserResponse user = userServiceClient.getUserById(request.userId());

        Order order = new Order();
        order.setUserId(user.id());
        order.setStatus("CREATED");
        order.setTotalPrice(BigDecimal.ZERO);
        order.setDeleted(false);

        List<OrderItem> orderItems = request.items().stream()
                .map(itemReq -> {
                    ItemResponse itemDto = itemService.getItemById(itemReq.itemId());
                    Item item = new Item();
                    item.setId(itemDto.id());
                    item.setName(itemDto.name());
                    item.setPrice(itemDto.price());

                    OrderItem orderItem = orderMapper.toOrderItemEntity(itemReq);
                    orderItem.setItem(item);
                    orderItem.setOrder(order);
                    return orderItem;
                })
                .collect(Collectors.toList());

        order.setOrderItems(orderItems);

        BigDecimal total = orderItems.stream()
                .map(orderItem -> orderItem.getItem()
                        .getPrice()
                        .multiply(BigDecimal.valueOf(orderItem.getQuantity()))
                )
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        order.setTotalPrice(total);

        Order saved = orderDAO.save(order);
        return orderMapper.toOrderDto(saved, user);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrderById(Long id) {
        validateId(id);

        Order order = orderDAO.findById(id)
                .orElseThrow(() -> new OrderNotFoundException("Order not found with id: " + id));

        if (order.isDeleted()) {
            throw new OrderAlreadyDeletedException("Order already deleted: " + id);
        }

        UserResponse user = userServiceClient.getUserById(order.getUserId());
        return orderMapper.toOrderDto(order, user);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrderResponse> getOrdersWithPaginationAndFilters(LocalDateTime from, LocalDateTime to, String status,
                                                                 Pageable pageable) {
        Specification<Order> specification = Specification
                .where(OrderSpecification.notDeleted())
                .and(OrderSpecification.createdAfter(from))
                .and(OrderSpecification.createdBefore(to))
                .and(OrderSpecification.hasStatus(status));

        Page<Order> orders = orderDAO.findAll(specification, pageable);

        return orders.map(order -> {
            UserResponse user = userServiceClient.getUserById(order.getUserId());
            return orderMapper.toOrderDto(order, user);
        });
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrderResponse> getOrdersByUserId(Long userId, Pageable pageable) {
        validateId(userId);

        Page<Order> orders = orderDAO.findByUserId(userId, pageable);
        UserResponse user = userServiceClient.getUserById(userId);

        return orders.map(order -> orderMapper.toOrderDto(order, user));
    }

    @Override
    @Transactional
    public OrderResponse updateOrderById(Long id, OrderUpdateRequest request) {
        validateId(id);

        Order order = orderDAO.findById(id)
                .orElseThrow(() -> new OrderNotFoundException("Order not found with id: " + id));

        if (order.isDeleted()) {
            throw new OrderAlreadyDeletedException("Order already deleted: " + id);
        }

        orderMapper.updateOrderEntityStatus(order, request);

        Order updated = orderDAO.save(order);
        UserResponse user = userServiceClient.getUserById(order.getUserId());

        return orderMapper.toOrderDto(updated, user);
    }

    @Override
    @Transactional
    public void deleteOrderById(Long id) {
        validateId(id);

        Order order = orderDAO.findById(id)
                .orElseThrow(() -> new OrderNotFoundException("Order not found with id: " + id));

        if (order.isDeleted()) {
            throw new OrderAlreadyDeletedException("Order already deleted: " + id);
        }

        orderDAO.softDeleteById(id);
    }

    private void validateId(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Id cannot be null");
        }

        if (id <= 0) {
            throw new IllegalArgumentException("Id must be positive");
        }
    }
}