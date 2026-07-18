package com.innowise.orderservice.integration;

import com.innowise.orderservice.model.dto.item.ItemResponse;
import com.innowise.orderservice.model.entity.Item;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ItemControllerIntegrationTest extends BaseIntegrationTest {

    @Test
    void getItemById_shouldReturnItem_whenExists() {
        Item savedItem = createItem();

        ResponseEntity<ItemResponse> response = restTemplate.getForEntity(
                baseUrl() + "/api/items/" + savedItem.getId(), ItemResponse.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        ItemResponse item = response.getBody();

        assertNotNull(item);
        assertEquals(savedItem.getId(), item.id());
        assertEquals(DEFAULT_ITEM_NAME, item.name());
        assertEquals(0, DEFAULT_ITEM_PRICE.compareTo(item.price()));
    }

    @Test
    void getItemById_shouldReturn404_whenItemDoesNotExist() {
        HttpClientErrorException exception = assertThrows(HttpClientErrorException.class,
                () -> restTemplate.getForEntity(baseUrl() + "/api/items/" + NON_EXISTENT_ID, ItemResponse.class));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertTrue(exception.getResponseBodyAsString().contains("Item not found"));
    }

    @Test
    void getItemById_shouldReturn400_whenIdIsInvalid() {
        HttpClientErrorException exception = assertThrows(HttpClientErrorException.class,
                () -> restTemplate.getForEntity(baseUrl() + "/api/items/" + INVALID_ID, ItemResponse.class));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertTrue(exception.getResponseBodyAsString().contains("Item id must be positive"));
    }
}