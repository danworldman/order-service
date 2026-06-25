package com.innowise.orderservice.service;

import com.innowise.orderservice.model.dto.order.OrderCreateRequest;
import com.innowise.orderservice.model.dto.order.OrderResponse;
import com.innowise.orderservice.model.dto.order.OrderUpdateRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

public interface OrderService {

    OrderResponse createOrder(OrderCreateRequest request);

    OrderResponse getOrderById(Long id);

    Page<OrderResponse> getOrdersWithPaginationAndFilters(LocalDateTime from, LocalDateTime to, List<String> statuses, Pageable pageable);

    Page<OrderResponse> getOrdersByUserId(Long userId, Pageable pageable);

    OrderResponse updateOrderById(Long id, OrderUpdateRequest request);

    void deleteOrderById(Long id);
}