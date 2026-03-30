package com.pm.notification.service;

import java.io.UnsupportedEncodingException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private SpringTemplateEngine templateEngine;

    public void sendWelcomeEmail(String toEmail, String customerName) throws MessagingException, UnsupportedEncodingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED, "UTF-8");

        // 1. Prepare the Data for the Template
        Context context = new Context();
        context.setVariable("customerName", customerName);
        context.setVariable("email", toEmail);

        // 2. Process the HTML Template
        String html = templateEngine.process("welcome-email", context);

        // 3. Set Mail Properties
        helper.setTo(toEmail);
        helper.setSubject("Welcome to Prince Mart! 👑");
        helper.setText(html, true);
        helper.setFrom("princemart.princeinc@gmail.com", "Prince Mart");

        // 4. Send
        mailSender.send(message);
    }
}