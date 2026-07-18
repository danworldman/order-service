package com.innowise.orderservice.model.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public abstract class EntityTestData {

    protected static final Long ITEM_ID_1 = 1L;
    protected static final Long ITEM_ID_2 = 2L;
    protected static final String ITEM_NAME = "Book";
    protected static final BigDecimal ITEM_PRICE = new BigDecimal("1000.00");

    protected static final Long ORDER_ID_1 = 100L;
    protected static final Long ORDER_ID_2 = 200L;
    protected static final Long USER_ID_1 = 1L;
    protected static final Long USER_ID_2 = 2L;
    protected static final OrderStatus STATUS_CREATED = OrderStatus.CREATED;
    protected static final OrderStatus STATUS_SHIPPED = OrderStatus.SHIPPED;

    protected static final Long ORDER_ITEM_ID_1 = 500L;
    protected static final Long ORDER_ITEM_ID_2 = 600L;
    protected static final Long QUANTITY_1 = 2L;
    protected static final Long QUANTITY_2 = 5L;
    protected static final LocalDateTime FIXED_LOCAL_DATE_TIME_CREATED = LocalDateTime.of(2026, 5, 19, 3, 1);
    protected static final LocalDateTime FIXED_LOCAL_DATE_TIME_UPDATED = LocalDateTime.of(2026, 5, 20, 3, 1);

    protected EntityTestData() {
    }
}