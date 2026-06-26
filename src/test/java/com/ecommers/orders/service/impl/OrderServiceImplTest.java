package com.ecommers.orders.service.impl;

import com.ecommers.orders.client.NotificationClient;
import com.ecommers.orders.client.ProductClient;
import com.ecommers.orders.dto.OrderDto.OrderRequest;
import com.ecommers.orders.dto.OrderDto.OrderResponse;
import com.ecommers.orders.dto.OrderDto.ProductDto;
import com.ecommers.orders.model.Order;
import com.ecommers.orders.repository.OrderRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias de la lógica de negocio de órdenes.
 * Se mockean el repositorio y los clientes Feign (product/notification)
 * para aislar la lógica del servicio, sin base de datos ni red.
 */
@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock
    private OrderRepository repository;
    @Mock
    private ProductClient productClient;
    @Mock
    private NotificationClient notificationClient;

    @InjectMocks
    private OrderServiceImpl service;

    @Test
    @DisplayName("createOrder: crea la orden en PENDING y calcula el total")
    void createOrder_creaOrdenPending() {
        // Given: un producto a 19.99
        when(productClient.getProductById(10L))
                .thenReturn(new ProductDto(10L, "Teclado", "Mecánico", 19.99));
        when(repository.save(any(Order.class))).thenAnswer(inv -> {
            Order o = inv.getArgument(0);
            o.setId(1L);
            return o;
        });

        // When: se crea una orden de 3 unidades
        OrderResponse response = service.createOrder(new OrderRequest(2L, 10L, 3));

        // Then: estado PENDING y total = 19.99 * 3 = 59.97
        assertThat(response.status()).isEqualTo("PENDING");
        assertThat(response.totalAmount()).isCloseTo(59.97, within(0.001));
        verify(repository).save(any(Order.class));
        verify(notificationClient).send(anyMap()); // se notifica ORDER_CREATED
    }

    @Test
    @DisplayName("updateStatus: un estado no permitido es rechazado sin tocar la base de datos")
    void updateStatus_estadoInvalido_lanzaExcepcion() {
        // When / Then: la validación ocurre antes de buscar la orden
        assertThatThrownBy(() -> service.updateStatus(1L, "ENTREGADO"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Estado invalido");
        verify(repository, never()).findById(any());
    }

    @Test
    @DisplayName("cancelOrder: una orden ya cancelada no se puede cancelar de nuevo")
    void cancelOrder_yaCancelada_lanzaExcepcion() {
        // Given: la orden 1 ya está CANCELLED
        Order cancelada = new Order();
        cancelada.setId(1L);
        cancelada.setStatus("CANCELLED");
        when(repository.findById(1L)).thenReturn(Optional.of(cancelada));

        // When / Then
        assertThatThrownBy(() -> service.cancelOrder(1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("ya está cancelada");
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("cancelOrder: una orden activa pasa a CANCELLED y se notifica al usuario")
    void cancelOrder_ordenActiva_seCancelaYNotifica() {
        // Given: la orden 1 está en PENDING
        Order pending = new Order();
        pending.setId(1L);
        pending.setUserId(2L);
        pending.setProductId(10L);
        pending.setQuantity(3);
        pending.setStatus("PENDING");
        when(repository.findById(1L)).thenReturn(Optional.of(pending));

        // When
        service.cancelOrder(1L);

        // Then: se guarda CANCELLED y se notifica
        assertThat(pending.getStatus()).isEqualTo("CANCELLED");
        verify(repository).save(pending);
        verify(notificationClient).send(anyMap());
    }

    @Test
    @DisplayName("updateStatus: PENDING -> CONFIRMED actualiza el estado")
    void updateStatus_aConfirmed_actualiza() {
        Order order = new Order();
        order.setId(1L);
        order.setProductId(10L);
        order.setQuantity(3);
        order.setStatus("PENDING");
        when(repository.findById(1L)).thenReturn(Optional.of(order));
        when(repository.save(any(Order.class))).thenAnswer(i -> i.getArgument(0));

        OrderResponse response = service.updateStatus(1L, "CONFIRMED");

        assertThat(response.status()).isEqualTo("CONFIRMED");
    }

    @Test
    @DisplayName("getOrderById / getOrdersByUser: devuelven datos mapeados")
    void getters_devuelvenDatos() {
        Order order = new Order();
        order.setId(1L);
        order.setUserId(2L);
        order.setProductId(10L);
        order.setQuantity(3);
        order.setTotalAmount(59.97);
        order.setStatus("PENDING");
        when(repository.findById(1L)).thenReturn(Optional.of(order));
        when(repository.findByUserId(2L)).thenReturn(java.util.List.of(order));

        assertThat(service.getOrderById(1L).id()).isEqualTo(1L);
        assertThat(service.getOrdersByUser(2L)).hasSize(1);
    }
}
