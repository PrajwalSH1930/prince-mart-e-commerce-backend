package com.pm.notification.controller;

import com.pm.notification.dto.NotificationRequest;
import com.pm.notification.service.NotificationService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping("/welcome")
    public String welcome() {
		return "Welcome!! This is Notification Service for Prince Mart by Prince Inc.";
	}
    
    @PostMapping("/order-confirmation")
    public String sendConfirmation(@RequestBody NotificationRequest request) {
        notificationService.sendOrderConfirmation(request);
        return "Notification Processed";
    }
    
    @GetMapping("/test-email")
    public String testEmail() {
        NotificationRequest testReq = new NotificationRequest();
        testReq.setRecipient("psh23g@gmail.com"); // Send to yourself
        testReq.setSubject("Prince Mart Test Email");
        testReq.setCustomerName("Prajwal");
        testReq.setOrderId("TEST-123");
        testReq.setAmount("500.00");
        
        notificationService.sendOrderConfirmation(testReq);
        return "Test email triggered! Check your console and inbox.";
    }
}