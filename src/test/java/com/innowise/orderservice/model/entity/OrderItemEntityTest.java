package com.innowise.orderservice.model.entity;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class OrderItemEntityTest extends EntityTestData {

    @Test
    void testEqualsAndHashCode() {
        OrderItem first = new OrderItem();
        first.setId(ORDER_ITEM_ID_1);
        first.setQuantity(QUANTITY_1);

        OrderItem second = new OrderItem();
        second.setId(ORDER_ITEM_ID_1);
        second.setQuantity(QUANTITY_2);

        OrderItem third = new OrderItem();
        third.setId(ORDER_ITEM_ID_2);
        third.setQuantity(QUANTITY_1);

        assertThat(first).isEqualTo(first);
        assertThat(first).isEqualTo(second);
        assertThat(second).isEqualTo(first);

        OrderItem fourth = new OrderItem();
        fourth.setId(ORDER_ITEM_ID_1);
        assertThat(first).isEqualTo(second);
        assertThat(second).isEqualTo(fourth);
        assertThat(first).isEqualTo(fourth);
        assertThat(first).isNotEqualTo(null);
        assertThat(first).isNotEqualTo(new Object());
        assertThat(first).isNotEqualTo(third);
        assertThat(third).isNotEqualTo(first);

        assertThat(first.hashCode()).isEqualTo(second.hashCode());
        assertThat(first.hashCode()).isNotEqualTo(third.hashCode());

        Set<OrderItem> set = new HashSet<>();
        set.add(first);
        assertThat(set).contains(second);
        assertThat(set).doesNotContain(third);
    }

    @Test
    void testEqualsWithNullId() {
        OrderItem first = new OrderItem();
        first.setId(null);
        OrderItem second = new OrderItem();
        second.setId(null);
        OrderItem third = new OrderItem();
        third.setId(ORDER_ITEM_ID_1);

        assertThat(first).isNotEqualTo(second);
        assertThat(first).isNotEqualTo(third);
    }

    @Test
    void testEqualsShouldIgnoreNonBusinessFields() {
        OrderItem item = new OrderItem();
        item.setId(ORDER_ITEM_ID_1);
        item.setQuantity(QUANTITY_1);
        item.setCreatedAt(FIXED_LOCAL_DATE_TIME_CREATED);
        item.setUpdatedAt(FIXED_LOCAL_DATE_TIME_UPDATED);

        OrderItem another = new OrderItem();
        another.setId(ORDER_ITEM_ID_1);
        another.setQuantity(QUANTITY_2);
        another.setCreatedAt(FIXED_LOCAL_DATE_TIME_CREATED);
        another.setUpdatedAt(FIXED_LOCAL_DATE_TIME_UPDATED);

        assertThat(item).isEqualTo(another);
        assertThat(item.hashCode()).isEqualTo(another.hashCode());
    }

    @Test
    void testHashCodeStability() {
        OrderItem item = new OrderItem();
        item.setId(ORDER_ITEM_ID_1);
        int first = item.hashCode();
        int second = item.hashCode();
        assertThat(first).isEqualTo(second);
    }

    @Test
    void testSetContainsAfterChangingNonBusinessFields() {
        OrderItem oldItem = new OrderItem();
        oldItem.setId(ORDER_ITEM_ID_1);
        oldItem.setQuantity(QUANTITY_2);

        OrderItem newItem = new OrderItem();
        newItem.setId(ORDER_ITEM_ID_1);
        newItem.setQuantity(QUANTITY_1);

        Set<OrderItem> set = new HashSet<>();
        set.add(oldItem);
        assertThat(set).contains(newItem);
    }
}