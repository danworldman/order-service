package com.innowise.orderservice.exception;

public class OrderAlreadyDeletedException extends RuntimeException {
    public OrderAlreadyDeletedException(String message) {
        super(message);
    }
}