package com.ecommers.orders.repository;

import com.ecommers.orders.model.Order;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface OrderRepository extends CrudRepository<Order, Long> {

    List<Order> findByUserId(Long userId);
}
