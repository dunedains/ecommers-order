package com.ecommers.orders.exception;

public class OrderNotFoundException extends RuntimeException {

    public OrderNotFoundException(Long id) {
        super("Orden no encontrada con id: " + id);
    }
}
