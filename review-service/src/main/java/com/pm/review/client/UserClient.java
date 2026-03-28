package com.pm.review.client;

import com.pm.review.dto.UserResponse; // We'll create this DTO next
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "IDENTITY-SERVICE")
public interface UserClient {

    @GetMapping("/auth/id/{userId}")
    UserResponse getUserById(@PathVariable("userId") Long userId);
}