package com.pm.notification.service;

import com.pm.notification.dto.NotificationRequest;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    private final JavaMailSender mailSender;

    public NotificationService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendOrderConfirmation(NotificationRequest request) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setFrom("princemart.princeinc@gmail.com");
            helper.setTo(request.getRecipient());
            helper.setSubject(request.getSubject());
            
            String htmlContent = "<h3>Hello " + request.getCustomerName() + "!</h3>" +
                    "<p>Thank you for shopping at <b>Prince Mart</b>.</p>" +
                    "<p>Your order <b>#" + request.getOrderId() + "</b> has been confirmed.</p>" +
                    "<p>Total Amount: <b>₹" + request.getAmount() + "</b></p>" +
                    "<br><p>We are preparing your package for shipment!</p>";

            helper.setText(htmlContent, true); // 'true' means it's HTML

            mailSender.send(message);
            System.out.println("Confirmation Email sent to " + request.getRecipient());
        } catch (Exception e) {
            System.err.println("Failed to send email: " + e.getMessage());
        }
    }
}