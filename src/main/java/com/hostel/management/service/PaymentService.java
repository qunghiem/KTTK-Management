    package com.hostel.management.service;

    import com.hostel.management.model.Payment;
    import com.hostel.management.model.Booking;
    import com.hostel.management.model.Room;
    import com.hostel.management.repository.PaymentRepository;
    import org.springframework.beans.factory.annotation.Autowired;
    import org.springframework.stereotype.Service;
    import com.hostel.management.repository.BookingRepository;
    import java.util.Date;
    import java.util.List;
    import java.util.UUID;

    @Service
    public class PaymentService {
        @Autowired
        private PaymentRepository paymentRepository;
        @Autowired
        private BookingRepository bookingRepository; // Thêm injection cho BookingRepository
        public Payment processPayment(Payment payment) {
            // Thiết lập ngày thanh toán
            payment.setPaymentDate(new Date());

            // Tạo mã giao dịch nếu chưa có
            if (payment.getTransactionCode() == null || payment.getTransactionCode().isEmpty()) {
                payment.setTransactionCode(generateTransactionCode());
            }

            // Cập nhật trạng thái booking và phòng
            if (payment.getBookingId() > 0) {
                Booking booking = bookingRepository.findById(payment.getBookingId()).orElse(null);
                if (booking != null) {
                    booking.setStatus("confirmed");

                    Room room = booking.getRoomId();
                    room.setStatus("booked");

                    bookingRepository.save(booking);
                }
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

        public Payment getPaymentByBookingId(int bookingId) {
            return paymentRepository.findByBookingId(bookingId);
        }
    }