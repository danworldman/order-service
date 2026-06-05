package com.innowise.orderservice.service;

import com.innowise.orderservice.model.dto.item.ItemResponse;
import com.innowise.orderservice.model.dto.user.UserResponse;
import com.innowise.orderservice.model.entity.Item;

import java.math.BigDecimal;

public abstract class ServiceTestData {

    protected static final Long DEFAULT_USER_ID = 1L;
    protected static final String DEFAULT_USER_NAME = "Bob";
    protected static final String DEFAULT_USER_SURNAME = "Duck";
    protected static final String DEFAULT_USER_EMAIL = "bob@email.com";

    protected static final Long DEFAULT_ITEM_ID = 10L;
    protected static final String DEFAULT_ITEM_NAME = "Book";
    protected static final BigDecimal DEFAULT_ITEM_PRICE = new BigDecimal("1000.00");

    protected static final Long ORDER_ID_1 = 100L;
    protected static final Long QUANTITY_1 = 2L;

    protected static final String STATUS_CREATED = "CREATED";
    protected static final String STATUS_SHIPPED = "SHIPPED";

    protected static final Long NON_EXISTENT_ID = 10000L;
    protected static final Long INVALID_ID_ZERO = 0L;
    protected static final Long INVALID_ID_NEGATIVE = -1L;

    protected final Item sampleItem;
    protected final ItemResponse sampleItemResponse;
    protected final UserResponse defaultUserResponse;

    protected ServiceTestData() {
        sampleItem = new Item();
        sampleItem.setId(DEFAULT_ITEM_ID);
        sampleItem.setName(DEFAULT_ITEM_NAME);
        sampleItem.setPrice(DEFAULT_ITEM_PRICE);

        sampleItemResponse = new ItemResponse(DEFAULT_ITEM_ID, DEFAULT_ITEM_NAME, DEFAULT_ITEM_PRICE);
        defaultUserResponse = new UserResponse(DEFAULT_USER_ID, DEFAULT_USER_NAME, DEFAULT_USER_SURNAME, DEFAULT_USER_EMAIL);
    }
}