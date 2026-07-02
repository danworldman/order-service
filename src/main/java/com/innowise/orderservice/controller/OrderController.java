package com.innowise.orderservice.controller;

import com.innowise.orderservice.model.dto.order.OrderCreateRequest;
import com.innowise.orderservice.model.dto.order.OrderResponse;
import com.innowise.orderservice.model.dto.order.OrderUpdateRequest;
import com.innowise.orderservice.service.OrderService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(
            @Valid @RequestBody OrderCreateRequest request,
            HttpServletRequest servletRequest) {
        String authHeader = servletRequest.getHeader(HttpHeaders.AUTHORIZATION);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(orderService.createOrder(request, authHeader));
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getOrderById(
            @PathVariable Long id,
            HttpServletRequest servletRequest) {
        String authHeader = servletRequest.getHeader(HttpHeaders.AUTHORIZATION);
        return ResponseEntity.ok(orderService.getOrderById(id, authHeader));
    }

    @GetMapping
    public ResponseEntity<Page<OrderResponse>> getOrdersWithPaginationAndFilters(
            @RequestParam(required = false) LocalDateTime from,
            @RequestParam(required = false) LocalDateTime to,
            @RequestParam(required = false) List<String> statuses,
            Pageable pageable,
            HttpServletRequest servletRequest) {
        String authHeader = servletRequest.getHeader(HttpHeaders.AUTHORIZATION);
        return ResponseEntity.ok(orderService.getOrdersWithPaginationAndFilters(from, to, statuses, pageable, authHeader));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<Page<OrderResponse>> getOrdersByUserId(
            @PathVariable Long userId,
            Pageable pageable,
            HttpServletRequest servletRequest) {
        String authHeader = servletRequest.getHeader(HttpHeaders.AUTHORIZATION);
        return ResponseEntity.ok(orderService.getOrdersByUserId(userId, pageable, authHeader));
    }

    @PutMapping("/{id}")
    public ResponseEntity<OrderResponse> updateOrderById(
            @PathVariable Long id,
            @Valid @RequestBody OrderUpdateRequest request,
            HttpServletRequest servletRequest) {
        String authHeader = servletRequest.getHeader(HttpHeaders.AUTHORIZATION);
        return ResponseEntity.ok(orderService.updateOrderById(id, request, authHeader));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOrderById(@PathVariable Long id) {
        orderService.deleteOrderById(id);
        return ResponseEntity.noContent().build();
    }
}