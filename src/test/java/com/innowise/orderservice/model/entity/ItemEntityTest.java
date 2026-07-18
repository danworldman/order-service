package com.innowise.orderservice.model.entity;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class ItemEntityTest extends EntityTestData {

    @Test
    void testEqualsAndHashCode() {
        Item first = new Item();
        first.setId(ITEM_ID_1);
        first.setName(ITEM_NAME);
        first.setPrice(ITEM_PRICE);

        Item second = new Item();
        second.setId(ITEM_ID_1);
        second.setName("Mouse");
        second.setPrice(BigDecimal.valueOf(25));

        Item third = new Item();
        third.setId(ITEM_ID_2);
        third.setName(ITEM_NAME);
        third.setPrice(ITEM_PRICE);

        assertThat(first).isEqualTo(first);
        assertThat(first).isEqualTo(second);
        assertThat(second).isEqualTo(first);

        Item fourth = new Item();
        fourth.setId(ITEM_ID_1);
        fourth.setName("Key");
        assertThat(first).isEqualTo(second);
        assertThat(second).isEqualTo(fourth);
        assertThat(first).isEqualTo(fourth);
        assertThat(first).isNotEqualTo(null);
        assertThat(first).isNotEqualTo(new Object());
        assertThat(first).isNotEqualTo(third);
        assertThat(third).isNotEqualTo(first);

        assertThat(first.hashCode()).isEqualTo(second.hashCode());
        assertThat(first.hashCode()).isNotEqualTo(third.hashCode());

        Set<Item> set = new HashSet<>();
        set.add(first);
        assertThat(set).contains(second);
        assertThat(set).doesNotContain(third);
    }

    @Test
    void testEqualsWithNullId() {
        Item first = new Item();
        first.setId(null);
        Item second = new Item();
        second.setId(null);
        Item third = new Item();
        third.setId(ITEM_ID_1);

        assertThat(first).isNotEqualTo(second);
        assertThat(first).isNotEqualTo(third);
    }

    @Test
    void testEqualsShouldIgnoreNonBusinessFields() {
        Item item = new Item();
        item.setId(ITEM_ID_1);
        item.setName(ITEM_NAME);
        item.setPrice(ITEM_PRICE);
        item.setCreatedAt(FIXED_LOCAL_DATE_TIME_CREATED);
        item.setUpdatedAt(FIXED_LOCAL_DATE_TIME_UPDATED);

        Item another = new Item();
        another.setId(ITEM_ID_1);
        another.setName("Bag");
        another.setPrice(BigDecimal.valueOf(700));
        another.setCreatedAt(FIXED_LOCAL_DATE_TIME_CREATED);
        another.setUpdatedAt(FIXED_LOCAL_DATE_TIME_UPDATED);

        assertThat(item).isEqualTo(another);
        assertThat(item.hashCode()).isEqualTo(another.hashCode());
    }

    @Test
    void testHashCodeStability() {
        Item item = new Item();
        item.setId(ITEM_ID_1);
        int first = item.hashCode();
        int second = item.hashCode();
        assertThat(first).isEqualTo(second);
    }

    @Test
    void testSetContainsAfterChangingNonBusinessFields() {
        Item oldItem = new Item();
        oldItem.setId(ITEM_ID_1);
        oldItem.setName("Old");
        oldItem.setPrice(BigDecimal.valueOf(10));

        Item newItem = new Item();
        newItem.setId(ITEM_ID_1);
        newItem.setName("New");
        newItem.setPrice(BigDecimal.valueOf(100));

        Set<Item> set = new HashSet<>();
        set.add(oldItem);
        assertThat(set).contains(newItem);
    }
}