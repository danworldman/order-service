package com.innowise.orderservice.dao;

import com.innowise.orderservice.model.entity.Item;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ItemDAO extends JpaRepository<Item, Long> {
}