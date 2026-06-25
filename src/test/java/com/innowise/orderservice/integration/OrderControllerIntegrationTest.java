package com.innowise.orderservice.integration;

import com.innowise.orderservice.model.dto.order.OrderCreateRequest;
import com.innowise.orderservice.model.dto.order.OrderItemCreateRequest;
import com.innowise.orderservice.model.dto.order.OrderItemResponse;
import com.innowise.orderservice.model.dto.order.OrderResponse;
import com.innowise.orderservice.model.dto.order.OrderUpdateRequest;
import com.innowise.orderservice.model.dto.user.UserResponse;
import com.innowise.orderservice.model.entity.Item;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;

import java.math.BigDecimal;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class OrderControllerIntegrationTest extends BaseIntegrationTest {

    private HttpHeaders createAuthHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set(HttpHeaders.AUTHORIZATION, "Bearer test-token");
        return headers;
    }

    @Test
    void createOrder_shouldReturn201Created_whenUserExists() {
        mockUserService();

        Item item1 = createItem();
        Item item2 = createItem();

        OrderCreateRequest request = new OrderCreateRequest(DEFAULT_USER_ID, List.of(
                new OrderItemCreateRequest(item1.getId(), 2L),
                new OrderItemCreateRequest(item2.getId(), 1L)
        ));

        HttpEntity<OrderCreateRequest> entity = new HttpEntity<>(request, createAuthHeaders());

        ResponseEntity<OrderResponse> response = restTemplate.postForEntity(
                baseUrl() + "/api/orders", entity, OrderResponse.class
        );

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        OrderResponse order = response.getBody();

        assertNotNull(order);
        assertNotNull(order.id());
        assertEquals(DEFAULT_USER_ID, order.user().id());
        assertEquals("CREATED", order.status());
        assertEquals(0, BigDecimal.valueOf(3000.00).compareTo(order.totalPrice()));
        assertEquals(2, order.items().size());

        OrderItemResponse first = order.items().get(0);
        OrderItemResponse second = order.items().get(1);

        if (first.item().id().equals(item1.getId())) {
            assertEquals(DEFAULT_ITEM_NAME, first.item().name());
            assertEquals(2L, first.quantity());
            assertEquals(DEFAULT_ITEM_NAME, second.item().name());
            assertEquals(1L, second.quantity());
        } else {
            assertEquals(DEFAULT_ITEM_NAME, first.item().name());
            assertEquals(1L, first.quantity());
            assertEquals(DEFAULT_ITEM_NAME, second.item().name());
            assertEquals(2L, second.quantity());
        }
    }

    @Test
    void createOrder_shouldReturn404NotFound_whenUserDoesNotExist() {
        stubUserNotFound(NON_EXISTENT_ID);

        OrderCreateRequest request = new OrderCreateRequest(NON_EXISTENT_ID,
                List.of(new OrderItemCreateRequest(1L, 2L)));

        HttpEntity<OrderCreateRequest> entity = new HttpEntity<>(request, createAuthHeaders());

        HttpClientErrorException exception = assertThrows(HttpClientErrorException.class, () -> {
            restTemplate.postForEntity(baseUrl() + "/api/orders", entity, OrderResponse.class);
        });

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertTrue(exception.getResponseBodyAsString().contains("User not found"));
    }

    @Test
    void createOrder_shouldReturn400BadRequest_whenOrderItemsListIsEmpty() {
        OrderCreateRequest request = new OrderCreateRequest(DEFAULT_USER_ID, List.of());

        HttpEntity<OrderCreateRequest> entity = new HttpEntity<>(request, createAuthHeaders());

        HttpClientErrorException exception = assertThrows(HttpClientErrorException.class, () -> {
            restTemplate.postForEntity(baseUrl() + "/api/orders", entity, OrderResponse.class);
        });

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    }

    @Test
    void getOrderById_shouldReturnOrder_whenExists() {
        mockUserService();
        Item item = createItem();

        OrderCreateRequest createRequest = new OrderCreateRequest(DEFAULT_USER_ID,
                List.of(new OrderItemCreateRequest(item.getId(), 1L)));

        HttpEntity<OrderCreateRequest> createEntity = new HttpEntity<>(createRequest, createAuthHeaders());
        OrderResponse createdOrder = restTemplate.postForObject(
                baseUrl() + "/api/orders", createEntity, OrderResponse.class
        );

        HttpEntity<Void> getEntity = new HttpEntity<>(createAuthHeaders());
        ResponseEntity<OrderResponse> getResponse = restTemplate.exchange(
                baseUrl() + "/api/orders/" + createdOrder.id(), HttpMethod.GET, getEntity, OrderResponse.class
        );

        assertEquals(HttpStatus.OK, getResponse.getStatusCode());
        OrderResponse order = getResponse.getBody();

        assertNotNull(order);
        assertEquals(createdOrder.id(), order.id());
        assertEquals(DEFAULT_USER_ID, order.user().id());
        assertEquals("CREATED", order.status());
        assertEquals(1, order.items().size());
        assertEquals(DEFAULT_ITEM_NAME, order.items().getFirst().item().name());
    }

    @Test
    void getOrderById_shouldReturn404_whenOrderDoesNotExist() {
        HttpEntity<Void> entity = new HttpEntity<>(createAuthHeaders());
        HttpClientErrorException exception = assertThrows(HttpClientErrorException.class, () -> {
            restTemplate.exchange(baseUrl() + "/api/orders/" + NON_EXISTENT_ID, HttpMethod.GET, entity, OrderResponse.class);
        });

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertTrue(exception.getResponseBodyAsString().contains("Order not found"));
    }

    @Test
    void getOrderById_shouldReturn400_whenOrderSoftDeleted() {
        mockUserService();
        Item item = createItem();

        OrderCreateRequest createRequest = new OrderCreateRequest(DEFAULT_USER_ID,
                List.of(new OrderItemCreateRequest(item.getId(), 1L)));

        HttpEntity<OrderCreateRequest> createEntity = new HttpEntity<>(createRequest, createAuthHeaders());
        OrderResponse created = restTemplate.postForObject(
                baseUrl() + "/api/orders", createEntity, OrderResponse.class
        );

        HttpEntity<Void> deleteEntity = new HttpEntity<>(createAuthHeaders());
        restTemplate.exchange(baseUrl() + "/api/orders/" + created.id(), HttpMethod.DELETE, deleteEntity, Void.class);

        HttpClientErrorException exception = assertThrows(HttpClientErrorException.class, () -> {
            restTemplate.exchange(baseUrl() + "/api/orders/" + created.id(), HttpMethod.GET, deleteEntity, OrderResponse.class);
        });

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertTrue(exception.getResponseBodyAsString().contains("already deleted"));
    }

    @Test
    void getOrders_withPaginationAndFilters_shouldReturnPage() {
        mockUserService();
        Item item = createItem();

        OrderCreateRequest createRequest = new OrderCreateRequest(DEFAULT_USER_ID,
                List.of(new OrderItemCreateRequest(item.getId(), 1L)));

        HttpEntity<OrderCreateRequest> createEntity = new HttpEntity<>(createRequest, createAuthHeaders());
        OrderResponse created = restTemplate.postForObject(
                baseUrl() + "/api/orders", createEntity, OrderResponse.class
        );

        OrderUpdateRequest updateRequest = new OrderUpdateRequest("CONFIRMED");
        HttpEntity<OrderUpdateRequest> entity = new HttpEntity<>(updateRequest, createAuthHeaders());
        restTemplate.exchange(baseUrl() + "/api/orders/" + created.id(),
                HttpMethod.PUT, entity, OrderResponse.class);

        String url = baseUrl() + "/api/orders?statuses=CONFIRMED&page=0&size=10";
        HttpEntity<Void> getEntity = new HttpEntity<>(createAuthHeaders());
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, getEntity, String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().contains("CONFIRMED"));
    }

    @Test
    void getOrdersByUserId_shouldReturnOrdersForSpecificUser() throws Exception {
        Long alternativeUserId = 20L;
        String alternativeUserEmail = "alt@email.com";
        mockUserService();

        UserResponse alternativeUser = new UserResponse(alternativeUserId, "Alternative", "User", alternativeUserEmail);

        wireMockServer.stubFor(get(urlEqualTo("/api/users/" + alternativeUserId))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .withBody(toJson(alternativeUser))));

        wireMockServer.stubFor(get(urlPathEqualTo("/api/users/by-email"))
                .withQueryParam("email", com.github.tomakehurst.wiremock.client.WireMock.equalTo(alternativeUserEmail))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .withBody(toJson(alternativeUser))));

        Item item = createItem();

        OrderCreateRequest request1 = new OrderCreateRequest(DEFAULT_USER_ID, List.of(new OrderItemCreateRequest(item.getId(), 1L)));
        HttpEntity<OrderCreateRequest> entity1 = new HttpEntity<>(request1, createAuthHeaders());
        restTemplate.postForObject(baseUrl() + "/api/orders", entity1, OrderResponse.class);

        OrderCreateRequest request2 = new OrderCreateRequest(alternativeUserId, List.of(new OrderItemCreateRequest(item.getId(), 1L)));
        HttpEntity<OrderCreateRequest> entity2 = new HttpEntity<>(request2, createAuthHeaders());
        restTemplate.postForObject(baseUrl() + "/api/orders", entity2, OrderResponse.class);

        String url = baseUrl() + "/api/orders/user/" + DEFAULT_USER_ID + "?page=0&size=10";
        HttpEntity<Void> getEntity = new HttpEntity<>(createAuthHeaders());
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, getEntity, String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        String jsonResponse = response.getBody();
        assertNotNull(jsonResponse);
        assertTrue(jsonResponse.contains("\"user\":{\"id\":" + DEFAULT_USER_ID));
        assertFalse(jsonResponse.contains("\"user\":{\"id\":" + alternativeUserId));
    }

    @Test
    void updateOrder_shouldChangeStatus_whenOrderExists() {
        mockUserService();
        Item item = createItem();

        OrderCreateRequest createRequest = new OrderCreateRequest(DEFAULT_USER_ID, List.of(new OrderItemCreateRequest(item.getId(), 1L)));
        HttpEntity createEntity = new HttpEntity<>(createRequest, createAuthHeaders());
        OrderResponse created = restTemplate.postForObject(baseUrl() + "/api/orders", createEntity, OrderResponse.class);

        OrderUpdateRequest updateRequest = new OrderUpdateRequest("SHIPPED");
        HttpEntity entity = new HttpEntity<>(updateRequest, createAuthHeaders());
        ResponseEntity<OrderResponse> updateResponse = restTemplate.exchange(baseUrl() + "/api/orders/" + created.id(), HttpMethod.PUT, entity, OrderResponse.class);

        assertEquals(HttpStatus.OK, updateResponse.getStatusCode());
        assertEquals("SHIPPED", updateResponse.getBody().status());
    }

    @Test
    void updateOrder_shouldReturn404_whenOrderNotFound() {
        OrderUpdateRequest updateRequest = new OrderUpdateRequest("SHIPPED");
        HttpEntity entity = new HttpEntity<>(updateRequest, createAuthHeaders());

        HttpClientErrorException exception = assertThrows(HttpClientErrorException.class, () ->
                restTemplate.exchange(baseUrl() + "/api/orders/" + NON_EXISTENT_ID, HttpMethod.PUT, entity, OrderResponse.class));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }

    @Test
    void deleteOrder_shouldSoftDelete_andThenNotReturn() {
        mockUserService();
        Item item = createItem();

        OrderCreateRequest createRequest = new OrderCreateRequest(DEFAULT_USER_ID, List.of(new OrderItemCreateRequest(item.getId(), 1L)));
        HttpEntity createEntity = new HttpEntity<>(createRequest, createAuthHeaders());
        OrderResponse created = restTemplate.postForObject(baseUrl() + "/api/orders", createEntity, OrderResponse.class);

        HttpEntity testEntity = new HttpEntity<>(createAuthHeaders());
        restTemplate.exchange(baseUrl() + "/api/orders/" + created.id(), HttpMethod.DELETE, testEntity, Void.class);

        HttpClientErrorException exception = assertThrows(HttpClientErrorException.class, () ->
                restTemplate.exchange(baseUrl() + "/api/orders/" + created.id(), HttpMethod.GET, testEntity, OrderResponse.class));

        assertTrue(exception.getStatusCode().is4xxClientError());
    }

    private void stubUserNotFound(Long userId) {
        wireMockServer.stubFor(get(urlEqualTo("/api/users/" + userId))
                .willReturn(aResponse().withStatus(404)));

        wireMockServer.stubFor(get(urlEqualTo("/api/users/by-email?email=" + DEFAULT_USER_EMAIL))
                .willReturn(aResponse().withStatus(404)));
    }
}