package com.ecommers.orders.service.impl;

import com.ecommers.orders.client.NotificationClient;
import com.ecommers.orders.client.ProductClient;
import com.ecommers.orders.dto.OrderDto.*;
import com.ecommers.orders.exception.OrderNotFoundException;
import com.ecommers.orders.model.Order;
import com.ecommers.orders.repository.OrderRepository;
import com.ecommers.orders.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private static final String STATUS_PENDING   = "PENDING";
    private static final String STATUS_CONFIRMED = "CONFIRMED";
    private static final String STATUS_CANCELLED = "CANCELLED";
    private static final List<String> VALID_STATUSES = List.of(STATUS_PENDING, STATUS_CONFIRMED, STATUS_CANCELLED);

    private final OrderRepository repository;
    private final ProductClient productClient;
    private final NotificationClient notificationClient;

    @Override
    @Transactional
    public OrderResponse createOrder(OrderRequest request) {
        log.info("Creando orden userId={}, productId={}, quantity={}", request.userId(), request.productId(), request.quantity());
        ProductDto product = productClient.getProductById(request.productId());

        double total = product.price() * request.quantity();

        Order order = new Order();
        order.setUserId(request.userId());
        order.setProductId(request.productId());
        order.setQuantity(request.quantity());
        order.setTotalAmount(total);
        order.setStatus(STATUS_PENDING);

        OrderResponse saved = toResponse(repository.save(order));
        log.info("Orden creada id={}, total={}", saved.id(), saved.totalAmount());
        notify(saved.userId(), "ORDER_CREATED",
                "Tu orden #" + saved.id() + " por $" + saved.totalAmount() + " fue creada y está pendiente de pago");
        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrderById(Long id) {
        log.info("Buscando orden id={}", id);
        return toResponse(findOrThrow(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getOrdersByUser(Long userId) {
        log.info("Listando órdenes del usuario userId={}", userId);
        return repository.findByUserId(userId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public OrderResponse updateStatus(Long id, String status) {
        log.info("Actualizando estado de orden id={} a status={}", id, status);
        if (status == null || status.isBlank()) {
            throw new IllegalArgumentException("El estado no puede estar vacio");
        }
        String upperStatus = status.toUpperCase();
        if (!VALID_STATUSES.contains(upperStatus)) {
            throw new IllegalArgumentException("Estado invalido: " + status + ". Valores permitidos: " + VALID_STATUSES);
        }
        Order order = findOrThrow(id);
        if (STATUS_CANCELLED.equals(order.getStatus())) {
            throw new IllegalArgumentException("No se puede modificar una orden cancelada");
        }
        order.setStatus(upperStatus);
        return toResponse(repository.save(order));
    }

    @Override
    @Transactional
    public void cancelOrder(Long id) {
        log.info("Cancelando orden id={}", id);
        Order order = findOrThrow(id);
        if (STATUS_CANCELLED.equals(order.getStatus())) {
            throw new IllegalArgumentException("La orden ya está cancelada");
        }
        order.setStatus(STATUS_CANCELLED);
        repository.save(order);
        log.info("Orden id={} cancelada exitosamente", id);
        notify(order.getUserId(), "ORDER_CANCELLED",
                "Tu orden #" + id + " fue cancelada");
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private void notify(Long userId, String type, String message) {
        try {
            notificationClient.send(Map.of("userId", userId, "type", type, "message", message));
        } catch (Exception e) {
            log.warn("No se pudo enviar notificación [{}] al usuario {}: {}", type, userId, e.getMessage());
        }
    }

    private Order findOrThrow(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException(id));
    }

    private OrderResponse toResponse(Order order) {
        return new OrderResponse(
                order.getId(),
                order.getUserId(),
                order.getProductId(),
                order.getQuantity(),
                order.getTotalAmount(),
                order.getStatus()
        );
    }
}
