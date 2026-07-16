package com.innowise.orderservice.service;

import com.innowise.orderservice.model.dto.order.OrderCreateRequest;
import com.innowise.orderservice.model.dto.order.OrderResponse;
import com.innowise.orderservice.model.dto.order.OrderUpdateRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service for managing orders.
 */
public interface OrderService {

    /**
     * Creates a new order from the given request.
     *
     * @param request    the order creation data
     * @param authHeader the authentication header
     * @return the created order with user and item details
     */
    OrderResponse createOrder(OrderCreateRequest request, String authHeader);

    /**
     * Retrieves an order by its id.
     *
     * @param id         the order id
     * @param authHeader the authentication header
     * @return the order with user details
     */
    OrderResponse getOrderById(Long id, String authHeader);

    /**
     * Retrieves a paginated list of orders filtered by date range and statuses.
     *
     * @param from       the start date (inclusive)
     * @param to         the end date (inclusive)
     * @param statuses   the statuses to filter by
     * @param pageable   the pagination information
     * @param authHeader the authentication header
     * @return a page of order responses
     */
    Page<OrderResponse> getOrdersWithPaginationAndFilters(LocalDateTime from, LocalDateTime to,
                                                          List<String> statuses, Pageable pageable,
                                                          String authHeader);

    /**
     * Retrieves a paginated list of orders for a specific user.
     *
     * @param userId     the user id
     * @param pageable   the pagination information
     * @param authHeader the authentication header
     * @return a page of order responses
     */
    Page<OrderResponse> getOrdersByUserId(Long userId, Pageable pageable, String authHeader);

    /**
     * Updates the status of an existing order.
     *
     * @param id         the order id
     * @param request    the update data
     * @param authHeader the authentication header
     * @return the updated order with user details
     */
    OrderResponse updateOrderById(Long id, OrderUpdateRequest request, String authHeader);

    /**
     * Soft-deletes an order by its id.
     *
     * @param id the order id
     */
    void deleteOrderById(Long id);
}