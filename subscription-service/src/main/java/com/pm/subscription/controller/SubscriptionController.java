package com.pm.subscription.controller;

import org.apache.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pm.subscription.dto.SubscriptionDTO;
import com.pm.subscription.entity.Subscription;
import com.pm.subscription.service.SubscriptionService;

@RestController
@RequestMapping("/subscriptions")
public class SubscriptionController {

	private final SubscriptionService subscriptionService;
	
	public SubscriptionController(SubscriptionService subscriptionService) {
		super();
		this.subscriptionService = subscriptionService;
	}
	
	@GetMapping("/welcome")
	public String welcome() {
		return "Welcome to the Subscription Service! 🎉";
	}
	
	@PostMapping("/subscribe")
	public ResponseEntity<String> subscribe(@RequestBody SubscriptionDTO subDto) {
		boolean success = subscriptionService.subscribe(subDto.getEmail());
		if (success) {
			return ResponseEntity.ok("Thank you for subscribing to the Prince Mart Registry!");
		} else {
			return ResponseEntity.status(HttpStatus.SC_CONFLICT).body("Email already subscribed.");
		}
	}
	
	@GetMapping("/all")
	public ResponseEntity<?> getAllSubscriptions() {
		return ResponseEntity.ok(subscriptionService.getAllSubscriptions());
	}
	
	@DeleteMapping("/unsubscribe/{subId}")
	public ResponseEntity<Subscription> unsubscribe(@PathVariable Long subId) {
		Subscription success = subscriptionService.deleteSubscription(subId);
		return success != null ? ResponseEntity.ok(success) : ResponseEntity.notFound().build();
	}
}
