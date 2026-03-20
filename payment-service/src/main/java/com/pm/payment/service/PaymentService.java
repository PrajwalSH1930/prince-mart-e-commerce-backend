package com.pm.payment.service;

import com.pm.payment.dto.PaymentRequest;
import com.pm.payment.dto.PaymentResponse;
import com.pm.payment.entity.Payment;
import com.pm.payment.repository.PaymentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;

    public PaymentService(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    @Transactional
    public PaymentResponse processPayment(PaymentRequest request) {
        // 1. Create a Payment Entity
        Payment payment = new Payment();
        payment.setOrderId(request.getOrderId());
        payment.setAmount(request.getAmount());
        payment.setCurrency(request.getCurrency());
        payment.setPaymentMethod(request.getPaymentMethod());
        
        // 2. Mocking a Transaction ID (We will replace this with real Gateway ID later)
        String mockTxnId = "TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        payment.setTransactionId(mockTxnId);
        
        // 3. Set Initial Status
        payment.setStatus("COMPLETED"); // For now, we assume success

        // 4. Save to pm_payment_db
        paymentRepository.save(payment);

        return PaymentResponse.builder()
                .transactionId(mockTxnId)
                .paymentStatus(payment.getStatus())
                .message("Payment processed successfully")
                .build();
    }
}