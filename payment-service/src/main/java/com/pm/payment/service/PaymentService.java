package com.pm.payment.service;

import com.pm.payment.client.OrderClient;
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

    @Value("${razorpay.key.id}")
    private String razorpayId;

    @Value("${razorpay.key.secret}")
    private String razorpaySecret;

    public PaymentService(PaymentRepository paymentRepository, OrderClient orderClient) {
        this.paymentRepository = paymentRepository;
        this.orderClient = orderClient;
    }

    @Transactional
    public PaymentResponse processPayment(PaymentRequest request) {
        try {
            RazorpayClient client = new RazorpayClient(razorpayId, razorpaySecret);

            // Razorpay expects amount in PAISE (1 INR = 100 Paise)
            int amountInPaise = request.getAmount().multiply(new BigDecimal(100)).intValue();

            JSONObject orderRequest = new JSONObject();
            orderRequest.put("amount", amountInPaise);
            orderRequest.put("currency", "INR"); // We can go back to INR now!
            orderRequest.put("receipt", "order_rcptid_" + request.getOrderId());

            // Create Order in Razorpay
            Order razorpayOrder = client.orders.create(orderRequest);
            String razorpayOrderId = razorpayOrder.get("id");

            // Save to DB
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

        // Call the Order Service Handshake we built earlier!
        orderClient.updateOrderStatus(payment.getOrderId(), "PAID", "CONFIRMED");
        
        System.out.println("Handshake successful for Order: " + payment.getOrderId());
    }
}