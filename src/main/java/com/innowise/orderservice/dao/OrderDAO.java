package com.innowise.orderservice.dao;

import com.innowise.orderservice.model.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface OrderDAO extends JpaRepository<Order, Long>, JpaSpecificationExecutor<Order> {

    @Query("SELECT o FROM Order o LEFT JOIN FETCH o.orderItems oi LEFT JOIN FETCH oi.item WHERE o.id = :id")
    Optional<Order> findById(@Param("id") Long id);

    Page<Order> findByUserId(Long userId, Pageable pageable);

    @Modifying
    @Query("UPDATE Order o SET o.deleted = true WHERE o.id = :id")
    void softDeleteById(@Param("id") Long id);
}