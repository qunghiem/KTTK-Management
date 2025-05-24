package com.hostel.management.repository;

import com.hostel.management.model.Payment;
import com.hostel.management.model.Customer;
import com.hostel.management.model.Invoice;
import com.hostel.management.model.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Integer> {
    List<Payment> findByInvoice(Invoice invoice);

    List<Payment> findByCustomerId(Customer customerId);

    Payment findByTransactionCode(String transactionCode);

    Payment findByBooking(Booking booking);

    // Thêm phương thức mới
    List<Payment> findByPaymentDateBetween(Date startDate, Date endDate);
}