package com.pm.subscription.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import com.pm.subscription.repository.SubscriptionRepository;
import com.pm.subscription.entity.Subscription;

import java.io.UnsupportedEncodingException;
import java.util.List;

@Service
public class SubscriptionService {
    
    private final SubscriptionRepository subscriptionRepository;
    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine; // Injected for HTML processing

    public SubscriptionService(SubscriptionRepository subscriptionRepository, 
                               JavaMailSender mailSender, 
                               TemplateEngine templateEngine) {
        this.subscriptionRepository = subscriptionRepository;
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
    }
    
    public boolean subscribe(String email) {
        if (subscriptionRepository.findByEmail(email).isPresent()) {
            return false; 
        }
        System.out.println("New subscription: " + email);
        // Save to the registry
        subscriptionRepository.save(new Subscription(null, email));
        
        // Trigger the Welcome Protocol
        try {
            sendWelcomeEmail(email);
        } catch (Exception e) {
            // Log the error but don't fail the registration
            System.err.println("Email protocol failed: " + e.getMessage());
        }
        
        return true; 
    }
    
    private void sendWelcomeEmail(String toEmail) throws MessagingException, UnsupportedEncodingException {
        // 1. Prepare Thymeleaf Context
        Context context = new Context();
        context.setVariable("email", toEmail);
        
        // 2. Process the HTML template
        String htmlContent = templateEngine.process("subscription", context);
        
        // 3. Create MimeMessage for HTML support
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        
        helper.setFrom("princemart.princeinc@gmail.com", "Prince Mart");
        helper.setTo(toEmail);
        helper.setSubject("Prince Mart | Subscription Authenticated");
        helper.setText(htmlContent, true); // 'true' enables HTML rendering
        
        mailSender.send(message);
    }

    
    public List<Subscription> getAllSubscriptions() {
		return subscriptionRepository.findAll();
	}
    
    public Subscription deleteSubscription(Long id) {
		var subscriptionOpt = subscriptionRepository.findById(id);
		if (subscriptionOpt.isEmpty()) {
			return null;
		}
		
		Subscription subscription = subscriptionOpt.get();
		subscriptionRepository.delete(subscription);
		return subscription;
	}
}