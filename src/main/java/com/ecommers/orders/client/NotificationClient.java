package com.ecommers.orders.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

@FeignClient(name = "notification-service-orders", url = "${feign.client.notification-url}")
public interface NotificationClient {

    @PostMapping("/api/notifications")
    void send(@RequestBody Map<String, Object> request);
}
