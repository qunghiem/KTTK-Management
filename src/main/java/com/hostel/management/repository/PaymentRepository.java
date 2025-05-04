package com.hostel.management.repository;

import com.hostel.management.model.Payment;
import com.hostel.management.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Integer> {
    List<Payment> findByInvoiceId(int invoiceId);

    List<Payment> findByCustomerId(Customer customerId);

    Payment findByTransactionCode(String transactionCode);
}