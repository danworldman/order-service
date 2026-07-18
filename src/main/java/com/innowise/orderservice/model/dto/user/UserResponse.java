package com.innowise.orderservice.model.dto.user;

public record UserResponse(
        Long id,
        String name,
        String surname,
        String email
) {}