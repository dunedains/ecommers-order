package com.ecommers.orders.client;

import com.ecommers.orders.dto.OrderDto.ProductDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "product-service", url = "${feign.client.product-url}")
public interface ProductClient {

    @GetMapping("/api/productos/{id}")
    ProductDto getProductById(@PathVariable Long id);
}
