package com.hostel.management.repository;

import com.hostel.management.model.Invoice;
import com.hostel.management.model.Room;
import com.hostel.management.model.Customer;
import com.hostel.management.model.UtilityReading;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Integer> {
    List<Invoice> findByRoom(Room room);
    List<Invoice> findByCustomer(Customer customer);
    List<Invoice> findByCreateDateBetween(Date startDate, Date endDate);
    List<Invoice> findByMonthAndYear(int month, int year);
    List<Invoice> findByPaidStatus(boolean paidStatus);
    Invoice findByUtilityReading(UtilityReading utilityReading);
}