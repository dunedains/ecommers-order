package com.ecommers.orders.controller;

import com.ecommers.orders.dto.OrderDto.OrderRequest;
import com.ecommers.orders.dto.OrderDto.OrderResponse;
import com.ecommers.orders.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService service;

    @PostMapping
    public ResponseEntity<EntityModel<OrderResponse>> createOrder(@Valid @RequestBody OrderRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(toModel(service.createOrder(request)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<EntityModel<OrderResponse>> getOrderById(@PathVariable Long id) {
        return ResponseEntity.ok(toModel(service.getOrderById(id)));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<CollectionModel<EntityModel<OrderResponse>>> getOrdersByUser(@PathVariable Long userId) {
        List<EntityModel<OrderResponse>> orders = service.getOrdersByUser(userId).stream()
                .map(this::toModel)
                .toList();
        return ResponseEntity.ok(CollectionModel.of(orders,
                linkTo(methodOn(OrderController.class).getOrdersByUser(userId)).withSelfRel()));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<EntityModel<OrderResponse>> updateStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        return ResponseEntity.ok(toModel(service.updateStatus(id, status)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancelOrder(@PathVariable Long id) {
        service.cancelOrder(id);
        return ResponseEntity.noContent().build();
    }

    private EntityModel<OrderResponse> toModel(OrderResponse order) {
        return EntityModel.of(order,
                linkTo(methodOn(OrderController.class).getOrderById(order.id())).withSelfRel(),
                linkTo(methodOn(OrderController.class).getOrdersByUser(order.userId())).withRel("user-orders"));
    }
}
