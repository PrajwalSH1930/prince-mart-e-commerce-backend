package com.pm.notification.service;

import com.pm.notification.dto.NotificationRequest;
import com.pm.notification.entity.Notification;
import com.pm.notification.repository.NotificationRepository;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Service
public class NotificationService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;
    private final NotificationRepository notificationRepository; // Added for persistence

    public NotificationService(JavaMailSender mailSender, 
                               TemplateEngine templateEngine, 
                               NotificationRepository notificationRepository) {
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
        this.notificationRepository = notificationRepository;
    }

    public void sendOrderConfirmation(NotificationRequest request) {
        // Initialize the log entity
        Notification log = new Notification();
        
        // We assume your NotificationRequest DTO now has a userId field. 
        // If not, we use the OrderId as a temporary link.
        log.setUserId(request.getUserId()); 
        log.setType("EMAIL");
        log.setTitle(request.getSubject());
        log.setMessage("Order #" + request.getOrderId() + " confirmation sent to " + request.getRecipient());
        log.setStatus("PENDING");

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom("princemart.princeinc@gmail.com", "Prince Mart");
            helper.setTo(request.getRecipient());
            helper.setSubject(request.getSubject());

            // 1. Prepare data for Thymeleaf
            Context context = new Context();
            context.setVariable("customerName", request.getCustomerName());
            context.setVariable("orderId", request.getOrderId());
            context.setVariable("amount", request.getAmount());

            // 2. Process template
            String htmlContent = templateEngine.process("order-confirmation", context);
            helper.setText(htmlContent, true); 

            // 3. Send Email
            mailSender.send(message);
            
            // 4. Update status on success
            log.setStatus("SENT");
            System.out.println("Professional Confirmation Email sent to " + request.getRecipient());
            
        } catch (Exception e) {
            // 5. Update status on failure
            log.setStatus("FAILED");
            System.err.println("Failed to send email: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // 6. Save the record to the database regardless of success/failure
            notificationRepository.save(log);
        }
    }
}