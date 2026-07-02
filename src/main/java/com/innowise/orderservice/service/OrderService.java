package com.innowise.orderservice.service;

import com.innowise.orderservice.model.dto.order.OrderCreateRequest;
import com.innowise.orderservice.model.dto.order.OrderResponse;
import com.innowise.orderservice.model.dto.order.OrderUpdateRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

public interface OrderService {

    OrderResponse createOrder(OrderCreateRequest request, String authHeader);

    OrderResponse getOrderById(Long id, String authHeader);

    Page<OrderResponse> getOrdersWithPaginationAndFilters(LocalDateTime from, LocalDateTime to,
                                                          List<String> statuses, Pageable pageable,
                                                          String authHeader);

    Page<OrderResponse> getOrdersByUserId(Long userId, Pageable pageable, String authHeader);

    OrderResponse updateOrderById(Long id, OrderUpdateRequest request, String authHeader);

    void deleteOrderById(Long id);
}