package com.ecommers.orders.controller;

import com.ecommers.orders.dto.OrderDto.OrderResponse;
import com.ecommers.orders.service.OrderService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OrderController.class)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OrderService service;

    private OrderResponse sample(String status) {
        return new OrderResponse(1L, 2L, 10L, 3, 59.97, status);
    }

    @Test
    @DisplayName("POST /api/orders válido -> 201")
    void create_devuelve201() throws Exception {
        when(service.createOrder(any())).thenReturn(sample("PENDING"));

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userId\":2,\"productId\":10,\"quantity\":3}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    @DisplayName("POST /api/orders sin productId -> 400")
    void create_invalido_devuelve400() throws Exception {
        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userId\":2,\"quantity\":3}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/orders/{id} -> 200")
    void getById_devuelve200() throws Exception {
        when(service.getOrderById(1L)).thenReturn(sample("PENDING"));

        mockMvc.perform(get("/api/orders/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @DisplayName("GET /api/orders/user/{userId} -> 200")
    void getByUser_devuelve200() throws Exception {
        when(service.getOrdersByUser(2L)).thenReturn(List.of(sample("PENDING")));

        mockMvc.perform(get("/api/orders/user/2"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("PATCH /api/orders/{id}/status -> 200")
    void updateStatus_devuelve200() throws Exception {
        when(service.updateStatus(eq(1L), eq("CONFIRMED"))).thenReturn(sample("CONFIRMED"));

        mockMvc.perform(patch("/api/orders/1/status").param("status", "CONFIRMED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CONFIRMED"));
    }

    @Test
    @DisplayName("DELETE /api/orders/{id} -> 204")
    void cancel_devuelve204() throws Exception {
        mockMvc.perform(delete("/api/orders/1"))
                .andExpect(status().isNoContent());
    }
}
