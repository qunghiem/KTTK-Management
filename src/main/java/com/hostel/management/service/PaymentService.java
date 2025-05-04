package com.hostel.management.service;

import com.hostel.management.model.Payment;
import com.hostel.management.repository.PaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
public class PaymentService {
    @Autowired
    private PaymentRepository paymentRepository;

    public Payment processPayment(Payment payment) {
        // Thiết lập ngày thanh toán
        payment.setPaymentDate(new Date());

        // Tạo mã giao dịch nếu chưa có
        if (payment.getTransactionCode() == null || payment.getTransactionCode().isEmpty()) {
            payment.setTransactionCode(generateTransactionCode());
        }

        return paymentRepository.save(payment);
    }

    public boolean verifyPayment(String transactionCode) {
        Payment payment = paymentRepository.findByTransactionCode(transactionCode);
        return payment != null;
    }

    public List<String> getPaymentMethods() {
        return List.of("bank_transfer", "credit_card", "momo", "zalopay");
    }

    public String generatePaymentQR(float amount, String description) {
        String qrData = "amount=" + amount + "&desc=" + description + "&timestamp=" + System.currentTimeMillis();
        return "qr_code_data_" + qrData.hashCode();
    }

    private String generateTransactionCode() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();
    }
}