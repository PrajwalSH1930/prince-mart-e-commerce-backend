package com.pm.auth.client;

import com.pm.auth.dto.NotificationRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "NOTIFICATION-SERVICE") // Ensure this matches the name in Eureka
public interface NotificationClient {

    @PostMapping("/notifications/welcome-email")
    void sendNotification(@RequestBody NotificationRequest request);
}