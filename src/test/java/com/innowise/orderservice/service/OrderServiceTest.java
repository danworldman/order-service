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

        Mockito.when(userServiceClient.getUserById(DEFAULT_USER_ID)).thenReturn(defaultUserResponse);
        Mockito.when(userServiceClient.getUserByEmail(DEFAULT_USER_EMAIL)).thenReturn(defaultUserResponse);
        Mockito.when(itemService.getItemById(DEFAULT_ITEM_ID)).thenReturn(sampleItemResponse);
        Mockito.when(itemMapper.toEntity(sampleItemResponse)).thenReturn(sampleItem);
        Mockito.when(orderMapper.toOrderItemEntity(Mockito.any(OrderItemCreateRequest.class))).thenReturn(orderItem);
        Mockito.when(orderDAO.save(Mockito.any(Order.class))).thenReturn(order);
        Mockito.when(orderMapper.toOrderDto(Mockito.any(Order.class), Mockito.eq(defaultUserResponse))).thenReturn(response);

        OrderResponse result = orderService.createOrder(request);

        assertThat(result).isNotNull();
        assertThat(result.totalPrice()).isEqualByComparingTo("2000.00");
        Mockito.verify(orderDAO).save(Mockito.any(Order.class));
    }

    @Test
    void createOrder_shouldThrowUserNotFoundException_whenUserNotFound() {
        OrderCreateRequest request = new OrderCreateRequest(DEFAULT_USER_ID,
                List.of(new OrderItemCreateRequest(DEFAULT_ITEM_ID, QUANTITY_1))
        );

        Mockito.when(userServiceClient.getUserById(DEFAULT_USER_ID)).thenReturn(defaultUserResponse);
        Mockito.when(userServiceClient.getUserByEmail(DEFAULT_USER_EMAIL))
                .thenThrow(new UserNotFoundException("User not found"));

        assertThatThrownBy(() -> orderService.createOrder(request))
                .isInstanceOf(UserNotFoundException.class);

        Mockito.verify(orderDAO, Mockito.never()).save(Mockito.any());
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
        Mockito.when(userServiceClient.getUserById(DEFAULT_USER_ID)).thenReturn(defaultUserResponse);
        Mockito.when(orderMapper.toOrderDto(order, defaultUserResponse)).thenReturn(response);

        OrderResponse result = orderService.getOrderById(ORDER_ID_1);

        assertThat(result.id()).isEqualTo(ORDER_ID_1);
    }

    @Test
    void getOrderById_shouldThrowOrderNotFoundException_whenNotExists() {
        Mockito.when(orderDAO.findById(NON_EXISTENT_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.getOrderById(NON_EXISTENT_ID))
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

        Mockito.verify(orderDAO, Mockito.never()).softDeleteById(Mockito.any());
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
        Mockito.when(userServiceClient.getUserById(DEFAULT_USER_ID)).thenReturn(defaultUserResponse);
        Mockito.when(orderDAO.save(order)).thenReturn(order);
        Mockito.when(orderMapper.toOrderDto(order, defaultUserResponse)).thenReturn(response);

        OrderResponse result = orderService.updateOrderById(ORDER_ID_1, updateRequest);

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

        Mockito.when(orderDAO.findAll(Mockito.any(Specification.class), Mockito.eq(pageable))).thenReturn(orderPage);
        Mockito.when(userServiceClient.getUserById(DEFAULT_USER_ID)).thenReturn(defaultUserResponse);
        Mockito.when(orderMapper.toOrderDto(order, defaultUserResponse)).thenReturn(response);

        Page<OrderResponse> result = orderService.getOrdersWithPaginationAndFilters(from, to, statuses, pageable);

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

        Mockito.when(orderDAO.findAll(Mockito.any(Specification.class), Mockito.eq(pageable))).thenReturn(orderPage);
        Mockito.when(userServiceClient.getUserById(DEFAULT_USER_ID)).thenReturn(defaultUserResponse);
        Mockito.when(orderMapper.toOrderDto(order, defaultUserResponse)).thenReturn(response);

        Page<OrderResponse> result = orderService.getOrdersByUserId(DEFAULT_USER_ID, pageable);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().getFirst().user().id()).isEqualTo(DEFAULT_USER_ID);
    }

    @Test
    void getOrdersByUserId_shouldThrowIllegalArgumentException_whenUserIdIsZero() {
        Pageable pageable = PageRequest.of(0, 10);

        assertThatThrownBy(() -> orderService.getOrdersByUserId(INVALID_ID_ZERO, pageable))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void getOrdersByUserId_shouldThrowIllegalArgumentException_whenUserIdIsNegative() {
        Pageable pageable = PageRequest.of(0, 10);

        assertThatThrownBy(() -> orderService.getOrdersByUserId(INVALID_ID_NEGATIVE, pageable))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
