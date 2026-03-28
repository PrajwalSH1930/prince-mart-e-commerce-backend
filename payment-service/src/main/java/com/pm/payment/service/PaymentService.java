package com.pm.payment.service;

import com.pm.payment.client.NotificationClient;
import com.pm.payment.client.OrderClient;
import com.pm.payment.dto.NotificationRequest;
import com.pm.payment.dto.OrderResponse;
import com.pm.payment.dto.PaymentRequest;
import com.pm.payment.dto.PaymentResponse;
import com.pm.payment.entity.Payment;
import com.pm.payment.repository.PaymentRepository;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderClient orderClient;
    private final NotificationClient notificationClient;

    @Value("${razorpay.key.id}")
    private String razorpayId;

    @Value("${razorpay.key.secret}")
    private String razorpaySecret;

    public PaymentService(PaymentRepository paymentRepository, 
                          OrderClient orderClient,
                          NotificationClient notificationClient) {
        this.paymentRepository = paymentRepository;
        this.orderClient = orderClient;
        this.notificationClient = notificationClient;
    }

    @Transactional
    public PaymentResponse processPayment(PaymentRequest request) {
        try {
            RazorpayClient client = new RazorpayClient(razorpayId, razorpaySecret);
            int amountInPaise = request.getAmount().multiply(new BigDecimal(100)).intValue();

            JSONObject orderRequest = new JSONObject();
            orderRequest.put("amount", amountInPaise);
            orderRequest.put("currency", "INR");
            orderRequest.put("receipt", "order_rcptid_" + request.getOrderId());

            Order razorpayOrder = client.orders.create(orderRequest);
            String razorpayOrderId = razorpayOrder.get("id");

            Payment payment = new Payment();
            payment.setOrderId(request.getOrderId());
            payment.setAmount(request.getAmount());
            payment.setCurrency("INR");
            payment.setPaymentMethod("RAZORPAY");
            payment.setTransactionId(razorpayOrderId);
            payment.setStatus("CREATED");
            paymentRepository.save(payment);

            return PaymentResponse.builder()
                    .transactionId(razorpayOrderId)
                    .paymentStatus("CREATED")
                    .message("Order Created Successfully")
                    .build();

        } catch (Exception e) {
            return PaymentResponse.builder()
                    .paymentStatus("FAILED")
                    .message(e.getMessage())
                    .build();
        }
    }

    @Transactional
    public void completePaymentManually(String razorpayOrderId, String paymentId) {
        Payment payment = paymentRepository.findByTransactionId(razorpayOrderId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));

        payment.setStatus("COMPLETED");
        paymentRepository.save(payment);

        // 1. Handshake: Get the real User Email and Name from Order Service
        OrderResponse orderInfo = orderClient.updateOrderStatus(payment.getOrderId(), "PAID", "CONFIRMED");
        System.out.println("Handshake successful for Order: " + payment.getOrderId());

        // 2. Trigger Notification with ACTUAL user data
        NotificationRequest emailReq = new NotificationRequest();
        
        // No more test emails! We use the data returned from Order Service
        emailReq.setRecipient(orderInfo.getUserEmail()); 
        
        emailReq.setSubject("Order Confirmed - Prince Mart");
        emailReq.setCustomerName(orderInfo.getCustomerName()); 
        emailReq.setOrderId(payment.getOrderId().toString());
        emailReq.setAmount(payment.getAmount().toString());

        try {
            System.out.println("Sending real email to: " + orderInfo.getUserEmail());
            notificationClient.sendConfirmation(emailReq);
            System.out.println("Notification sent successfully!");
        } catch (Exception e) {
            System.err.println("Non-critical Error: Failed to send email - " + e.getMessage());
        }
    }
}