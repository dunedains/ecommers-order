package com.ecommers.orders.exception;

public record ErrorResponse(int status, String message, String timestamp) {
}
