package com.hostel.management.service;

import com.hostel.management.model.Payment;
import com.hostel.management.model.Booking;
import com.hostel.management.model.Room;
import com.hostel.management.model.Invoice;
import com.hostel.management.model.Customer;
import com.hostel.management.repository.PaymentRepository;
import com.hostel.management.repository.BookingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
public class PaymentService {
    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private BookingRepository bookingRepository;

    public Payment processPayment(Payment payment) {
        // Thiết lập ngày thanh toán
        payment.setPaymentDate(new Date());

        // Tạo mã giao dịch nếu chưa có
        if (payment.getTransactionCode() == null || payment.getTransactionCode().isEmpty()) {
            payment.setTransactionCode(generateTransactionCode());
        }

        // Cập nhật trạng thái booking và phòng
        if (payment.getBooking() != null) {
            Booking booking = payment.getBooking();
            booking.setStatus("confirmed");

            Room room = booking.getRoomId();
            room.setStatus("booked");

            bookingRepository.save(booking);
        }

        return paymentRepository.save(payment);
    }

    public Payment createPayment(Payment payment) {
        return paymentRepository.save(payment);
    }

    public Payment updatePayment(Payment payment) {
        Payment existingPayment = paymentRepository.findById(payment.getId()).orElse(null);
        if (existingPayment == null) {
            throw new RuntimeException("Không tìm thấy thông tin thanh toán");
        }
        return paymentRepository.save(payment);
    }

    public Payment getPaymentById(int id) {
        return paymentRepository.findById(id).orElse(null);
    }

    public List<Payment> getPaymentsByCustomer(Customer customer) {
        return paymentRepository.findByCustomerId(customer);
    }

    public List<Payment> getPaymentsByInvoice(Invoice invoice) {
        return paymentRepository.findByInvoice(invoice);
    }

    public Payment getPaymentByBooking(Booking booking) {
        return paymentRepository.findByBooking(booking);
    }

    public Payment getPaymentByTransactionCode(String transactionCode) {
        return paymentRepository.findByTransactionCode(transactionCode);
    }

    public List<Payment> getPaymentsBetweenDates(Date startDate, Date endDate) {
        return paymentRepository.findByPaymentDateBetween(startDate, endDate);
    }

    public boolean verifyPayment(Payment payment) {
        Payment existingPayment = paymentRepository.findByTransactionCode(payment.getTransactionCode());
        return existingPayment != null;
    }

    public List<String> getPaymentMethods() {
        return List.of("bank_transfer", "credit_card", "momo", "zalopay");
    }

    public String generatePaymentQR(Payment payment) {
        String qrData = "amount=" + payment.getAmount() +
                "&desc=" + generatePaymentDescription(payment) +
                "&timestamp=" + System.currentTimeMillis();
        return "qr_code_data_" + qrData.hashCode();
    }

    private String generatePaymentDescription(Payment payment) {
        if (payment.getBooking() != null) {
            return "Đặt cọc phòng " + payment.getBooking().getRoomId().getRoomNumber();
        } else if (payment.getInvoice() != null) {
            return "Thanh toán hóa đơn " + payment.getInvoice().getId();
        }
        return "Thanh toán dịch vụ";
    }

    private String generateTransactionCode() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();
    }

    public void deletePayment(Payment payment) {
        paymentRepository.delete(payment);
    }
}