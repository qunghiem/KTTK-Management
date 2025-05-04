package com.hostel.management.repository;

import com.hostel.management.model.Customer;
import com.hostel.management.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Integer> {
    Customer findByUser(User user);

    Customer findByPhoneNumber(String phoneNumber);

    boolean existsByPhoneNumber(String phoneNumber);
}