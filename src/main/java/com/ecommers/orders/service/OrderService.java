package com.ecommers.orders.service;

import com.ecommers.orders.dto.OrderDto.OrderRequest;
import com.ecommers.orders.dto.OrderDto.OrderResponse;

import java.util.List;

public interface OrderService {

    OrderResponse createOrder(OrderRequest request);

    OrderResponse getOrderById(Long id);

    List<OrderResponse> getOrdersByUser(Long userId);

    OrderResponse updateStatus(Long id, String status);

    void cancelOrder(Long id);
}
