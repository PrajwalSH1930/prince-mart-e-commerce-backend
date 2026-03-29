package com.pm.notification.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pm.notification.client.AuditClient;
import com.pm.notification.dto.AuditLogRequest;
import com.pm.notification.dto.NotificationRequest;
import com.pm.notification.entity.Notification;
import com.pm.notification.repository.NotificationRepository;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.List;

@Service
public class NotificationService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;
    private final NotificationRepository notificationRepository;
    private final AuditClient auditClient;
    private final ObjectMapper objectMapper;

    public NotificationService(JavaMailSender mailSender, 
                               TemplateEngine templateEngine, 
                               NotificationRepository notificationRepository,
                               AuditClient auditClient,
                               ObjectMapper objectMapper) {
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
        this.notificationRepository = notificationRepository;
        this.auditClient = auditClient;
        this.objectMapper = objectMapper;
    }
    
    public List<Notification> getNotificationsForUser(Long userId) {
        return notificationRepository.findByUserIdOrderBySentAtDesc(userId);
    }
    
    public List<Notification> getAllNotifications() {
        return notificationRepository.findAll();
    }

    public void sendOrderConfirmation(NotificationRequest request) {
        // Initialize the log entity for internal persistence
        Notification log = new Notification();
        log.setUserId(request.getUserId()); 
        log.setType("EMAIL");
        log.setTitle(request.getSubject());
        log.setMessage("Order #" + request.getOrderId() + " confirmation sent to " + request.getRecipient());
        log.setStatus("PENDING");

        // Capture Request Data for Audit Before Attempt
        String dataBefore = null;
        try {
            dataBefore = objectMapper.writeValueAsString(request);
        } catch (Exception e) {
            System.err.println("JSON Conversion Error: " + e.getMessage());
        }

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
            
            // External Audit for Success
            sendAuditLog(request.getUserId(), "EMAIL_SENT_SUCCESS", dataBefore, "Status: SENT");
            
        } catch (Exception e) {
            // 5. Update status on failure
            log.setStatus("FAILED");
            System.err.println("Failed to send email: " + e.getMessage());
            
            // External Audit for Failure
            sendAuditLog(request.getUserId(), "EMAIL_SENT_FAILURE", dataBefore, "Error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // 6. Save the record to the local notification database
            notificationRepository.save(log);
        }
    }

    // Centralized Helper for External Auditing
    private void sendAuditLog(Long userId, String action, Object dataBefore, Object dataAfter) {
        try {
            String before = dataBefore != null ? (dataBefore instanceof String ? (String) dataBefore : objectMapper.writeValueAsString(dataBefore)) : null;
            String after = dataAfter != null ? (dataAfter instanceof String ? (String) dataAfter : objectMapper.writeValueAsString(dataAfter)) : null;

            auditClient.createLog(new AuditLogRequest(
                "NOTIFICATION-SERVICE", 
                action, 
                userId, 
                before, 
                after
            ));
        } catch (Exception e) {
            System.err.println("Audit logging failed in Notification Service: " + e.getMessage());
        }
    }
}