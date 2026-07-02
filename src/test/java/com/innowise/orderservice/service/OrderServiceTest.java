package com.innowise.orderservice.service;

import com.innowise.orderservice.client.UserServiceClient;
import com.innowise.orderservice.dao.OrderDAO;
import com.innowise.orderservice.exception.OrderAlreadyDeletedException;
import com.innowise.orderservice.exception.OrderNotFoundException;
import com.innowise.orderservice.exception.UserNotFoundException;
import com.innowise.orderservice.mapper.ItemMapper;
import com.innowise.orderservice.mapper.OrderMapper;
import com.innowise.orderservice.model.dto.order.OrderCreateRequest;
import com.innowise.orderservice.model.dto.order.OrderItemCreateRequest;
import com.innowise.orderservice.model.dto.order.OrderResponse;
import com.innowise.orderservice.model.dto.order.OrderUpdateRequest;
import com.innowise.orderservice.model.entity.Order;
import com.innowise.orderservice.model.entity.OrderItem;
import com.innowise.orderservice.service.impl.OrderServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest extends ServiceTestData {

    @Mock
    private OrderDAO orderDAO;

    @Mock
    private ItemService itemService;

    @Mock
    private UserServiceClient userServiceClient;

    @Mock
    private OrderMapper orderMapper;

    @Mock
    private ItemMapper itemMapper;

    @InjectMocks
    private OrderServiceImpl orderService;

    @Test
    void createOrder_shouldReturnOrderResponse_whenValid() {
        OrderCreateRequest request = new OrderCreateRequest(DEFAULT_USER_ID,
                List.of(new OrderItemCreateRequest(DEFAULT_ITEM_ID, QUANTITY_1))
        );

        OrderItem orderItem = new OrderItem();
        orderItem.setQuantity(QUANTITY_1);
        orderItem.setItem(sampleItem);

        Order order = new Order();
        order.setId(ORDER_ID_1);

        OrderResponse response = new OrderResponse(ORDER_ID_1, defaultUserResponse, STATUS_CREATED,
                BigDecimal.valueOf(2000.00), List.of(),
                LocalDateTime.of(2026, 5, 19, 3, 1),
                LocalDateTime.of(2026, 5, 19, 3, 1)
        );

        Mockito.lenient().when(userServiceClient.getUserById(anyLong(), anyString()))
                .thenReturn(defaultUserResponse);
        Mockito.when(itemService.getItemById(DEFAULT_ITEM_ID)).thenReturn(sampleItemResponse);
        Mockito.when(itemMapper.toEntity(sampleItemResponse)).thenReturn(sampleItem);
        Mockito.when(orderMapper.toOrderItemEntity(any(OrderItemCreateRequest.class))).thenReturn(orderItem);
        Mockito.when(orderDAO.save(any(Order.class))).thenReturn(order);
        Mockito.lenient().when(orderMapper.toOrderDto(any(Order.class), eq(defaultUserResponse))).thenReturn(response);

        OrderResponse result = orderService.createOrder(request, null);

        assertThat(result).isNotNull();
        assertThat(result.totalPrice()).isEqualByComparingTo("2000.00");
        Mockito.verify(orderDAO).save(any(Order.class));
    }

    @Test
    void createOrder_shouldThrowUserNotFoundException_whenUserNotFound() {
        OrderCreateRequest request = new OrderCreateRequest(NON_EXISTENT_ID,
                List.of(new OrderItemCreateRequest(DEFAULT_ITEM_ID, QUANTITY_1))
        );

        Mockito.lenient().when(userServiceClient.getUserById(eq(NON_EXISTENT_ID), anyString()))
                .thenThrow(new UserNotFoundException("User not found with id: " + NON_EXISTENT_ID));

        assertThatThrownBy(() -> orderService.createOrder(request, null))
                .isInstanceOf(UserNotFoundException.class);

        Mockito.verify(orderDAO, Mockito.never()).save(any());
    }

    @Test
    void getOrderById_shouldReturnOrder_whenExists() {
        Order order = new Order();
        order.setId(ORDER_ID_1);
        order.setUserId(DEFAULT_USER_ID);
        order.setDeleted(false);

        OrderResponse response = new OrderResponse(ORDER_ID_1, defaultUserResponse, STATUS_CREATED,
                BigDecimal.ZERO, List.of(), null, null
        );

        Mockito.when(orderDAO.findById(ORDER_ID_1)).thenReturn(Optional.of(order));
        Mockito.lenient().when(userServiceClient.getUserById(anyLong(), anyString()))
                .thenReturn(defaultUserResponse);
        Mockito.lenient().when(orderMapper.toOrderDto(any(Order.class), eq(defaultUserResponse))).thenReturn(response);

        OrderResponse result = orderService.getOrderById(ORDER_ID_1, null);

        assertThat(result.id()).isEqualTo(ORDER_ID_1);
    }

    @Test
    void getOrderById_shouldThrowOrderNotFoundException_whenNotExists() {
        Mockito.when(orderDAO.findById(NON_EXISTENT_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.getOrderById(NON_EXISTENT_ID, null))
                .isInstanceOf(OrderNotFoundException.class);
    }

    @Test
    void deleteOrder_shouldSoftDelete_whenExists() {
        Order order = new Order();
        order.setDeleted(false);
        Mockito.when(orderDAO.findById(ORDER_ID_1)).thenReturn(Optional.of(order));

        orderService.deleteOrderById(ORDER_ID_1);

        Mockito.verify(orderDAO).softDeleteById(ORDER_ID_1);
    }

    @Test
    void deleteOrder_shouldThrowOrderAlreadyDeletedException_whenAlreadyDeleted() {
        Order order = new Order();
        order.setDeleted(true);
        Mockito.when(orderDAO.findById(ORDER_ID_1)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.deleteOrderById(ORDER_ID_1))
                .isInstanceOf(OrderAlreadyDeletedException.class);

        Mockito.verify(orderDAO, Mockito.never()).softDeleteById(any());
    }

    @Test
    void updateOrderStatus_shouldUpdateAndReturn() {
        Order order = new Order();
        order.setId(ORDER_ID_1);
        order.setUserId(DEFAULT_USER_ID);
        order.setDeleted(false);

        OrderUpdateRequest updateRequest = new OrderUpdateRequest(STATUS_SHIPPED);
        OrderResponse response = new OrderResponse(ORDER_ID_1, defaultUserResponse, STATUS_SHIPPED,
                BigDecimal.ZERO, List.of(), null, null
        );

        Mockito.when(orderDAO.findById(ORDER_ID_1)).thenReturn(Optional.of(order));
        Mockito.lenient().when(userServiceClient.getUserById(anyLong(), anyString()))
                .thenReturn(defaultUserResponse);
        Mockito.when(orderDAO.save(order)).thenReturn(order);
        Mockito.lenient().when(orderMapper.toOrderDto(any(Order.class), eq(defaultUserResponse))).thenReturn(response);

        OrderResponse result = orderService.updateOrderById(ORDER_ID_1, updateRequest, null);

        assertThat(result.status()).isEqualTo(STATUS_SHIPPED);
        Mockito.verify(orderMapper).updateOrderEntityStatus(order, updateRequest);
    }

    @Test
    @SuppressWarnings("unchecked")
    void getOrdersWithPaginationAndFilters_shouldReturnPaginatedOrders() {
        LocalDateTime from = LocalDateTime.now().minusDays(1);
        LocalDateTime to = LocalDateTime.now();
        List<String> statuses = List.of(STATUS_CREATED);
        Pageable pageable = PageRequest.of(0, 10);

        Order order = new Order();
        order.setId(ORDER_ID_1);
        order.setUserId(DEFAULT_USER_ID);

        OrderResponse response = new OrderResponse(ORDER_ID_1, defaultUserResponse, STATUS_CREATED,
                BigDecimal.ZERO, List.of(), null, null
        );

        Page<Order> orderPage = new PageImpl<>(List.of(order));

        Mockito.when(orderDAO.findAll(any(Specification.class), eq(pageable))).thenReturn(orderPage);
        Mockito.lenient().when(userServiceClient.getUserById(anyLong(), anyString()))
                .thenReturn(defaultUserResponse);
        Mockito.lenient().when(orderMapper.toOrderDto(any(Order.class), eq(defaultUserResponse))).thenReturn(response);

        Page<OrderResponse> result = orderService.getOrdersWithPaginationAndFilters(from, to, statuses, pageable, null);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().getFirst().id()).isEqualTo(ORDER_ID_1);
    }

    @Test
    @SuppressWarnings("unchecked")
    void getOrdersByUserId_shouldReturnFilteredAndPaginatedOrders_whenValidUserId() {
        Pageable pageable = PageRequest.of(0, 10);

        Order order = new Order();
        order.setId(ORDER_ID_1);
        order.setUserId(DEFAULT_USER_ID);
        order.setDeleted(false);

        OrderResponse response = new OrderResponse(ORDER_ID_1, defaultUserResponse, STATUS_CREATED,
                BigDecimal.ZERO, List.of(), null, null
        );

        Page<Order> orderPage = new PageImpl<>(List.of(order));

        Mockito.when(orderDAO.findAll(any(Specification.class), eq(pageable))).thenReturn(orderPage);
        Mockito.lenient().when(userServiceClient.getUserById(anyLong(), anyString()))
                .thenReturn(defaultUserResponse);
        Mockito.lenient().when(orderMapper.toOrderDto(any(Order.class), eq(defaultUserResponse))).thenReturn(response);

        Page<OrderResponse> result = orderService.getOrdersByUserId(DEFAULT_USER_ID, pageable, null);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().getFirst().user().id()).isEqualTo(DEFAULT_USER_ID);
    }

    @Test
    void getOrdersByUserId_shouldThrowIllegalArgumentException_whenUserIdIsZero() {
        Pageable pageable = PageRequest.of(0, 10);

        assertThatThrownBy(() -> orderService.getOrdersByUserId(INVALID_ID_ZERO, pageable, null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void getOrdersByUserId_shouldThrowIllegalArgumentException_whenUserIdIsNegative() {
        Pageable pageable = PageRequest.of(0, 10);

        assertThatThrownBy(() -> orderService.getOrdersByUserId(INVALID_ID_NEGATIVE, pageable, null))
                .isInstanceOf(IllegalArgumentException.class);
    }
}