package com.innowise.orderservice.model.entity;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class OrderEntityTest extends EntityTestData {

    @Test
    void testEqualsAndHashCode() {
        Order first = new Order();
        first.setId(ORDER_ID_1);
        first.setUserId(USER_ID_1);
        first.setStatus(STATUS_CREATED);
        first.setTotalPrice(BigDecimal.valueOf(100));
        first.setDeleted(false);

        Order second = new Order();
        second.setId(ORDER_ID_1);
        second.setUserId(USER_ID_2);
        second.setStatus(STATUS_SHIPPED);
        second.setTotalPrice(BigDecimal.valueOf(200));
        second.setDeleted(true);

        Order third = new Order();
        third.setId(ORDER_ID_2);
        third.setUserId(USER_ID_1);
        third.setStatus(STATUS_CREATED);
        third.setTotalPrice(BigDecimal.valueOf(100));
        third.setDeleted(false);

        assertThat(first).isEqualTo(first);
        assertThat(first).isEqualTo(second);
        assertThat(second).isEqualTo(first);

        Order fourth = new Order();
        fourth.setId(ORDER_ID_1);
        assertThat(first).isEqualTo(second);
        assertThat(second).isEqualTo(fourth);
        assertThat(first).isEqualTo(fourth);
        assertThat(first).isNotEqualTo(null);
        assertThat(first).isNotEqualTo(new Object());
        assertThat(first).isNotEqualTo(third);
        assertThat(third).isNotEqualTo(first);

        assertThat(first.hashCode()).isEqualTo(second.hashCode());
        assertThat(first.hashCode()).isNotEqualTo(third.hashCode());

        Set<Order> set = new HashSet<>();
        set.add(first);
        assertThat(set).contains(second);
        assertThat(set).doesNotContain(third);
    }

    @Test
    void testEqualsWithNullId() {
        Order first = new Order();
        first.setId(null);
        Order second = new Order();
        second.setId(null);
        Order third = new Order();
        third.setId(ORDER_ID_1);

        assertThat(first).isNotEqualTo(second);
        assertThat(first).isNotEqualTo(third);
    }

    @Test
    void testEqualsShouldIgnoreNonBusinessFields() {
        Order order = new Order();
        order.setId(ORDER_ID_1);
        order.setUserId(USER_ID_1);
        order.setStatus(OrderStatus.PENDING);
        order.setTotalPrice(BigDecimal.valueOf(50));
        order.setDeleted(false);
        order.setCreatedAt(FIXED_LOCAL_DATE_TIME_CREATED);
        order.setUpdatedAt(FIXED_LOCAL_DATE_TIME_UPDATED);

        Order another = new Order();
        another.setId(ORDER_ID_1);
        another.setUserId(USER_ID_2);
        another.setStatus(OrderStatus.CONFIRMED);
        another.setTotalPrice(BigDecimal.valueOf(99));
        another.setDeleted(true);
        another.setCreatedAt(FIXED_LOCAL_DATE_TIME_CREATED);
        another.setUpdatedAt(FIXED_LOCAL_DATE_TIME_UPDATED);

        assertThat(order).isEqualTo(another);
        assertThat(order.hashCode()).isEqualTo(another.hashCode());
    }

    @Test
    void testHashCodeStability() {
        Order order = new Order();
        order.setId(ORDER_ID_1);
        int first = order.hashCode();
        int second = order.hashCode();
        assertThat(first).isEqualTo(second);
    }

    @Test
    void testSetContainsAfterChangingNonBusinessFields() {
        Order oldOrder = new Order();
        oldOrder.setId(ORDER_ID_1);
        oldOrder.setStatus(STATUS_CREATED);

        Order newOrder = new Order();
        newOrder.setId(ORDER_ID_1);
        newOrder.setStatus(OrderStatus.DELIVERED);

        Set<Order> set = new HashSet<>();
        set.add(oldOrder);
        assertThat(set).contains(newOrder);
    }
}