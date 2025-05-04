package com.hostel.management.service;

import com.hostel.management.model.Customer;
import com.hostel.management.model.User;
import com.hostel.management.repository.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CustomerService {
    @Autowired
    private CustomerRepository customerRepository;

    public Customer createCustomer(Customer customer) {
        // Kiểm tra số điện thoại đã tồn tại chưa
        if (customerRepository.existsByPhoneNumber(customer.getPhoneNumber())) {
            throw new RuntimeException("Số điện thoại đã được sử dụng");
        }

        return customerRepository.save(customer);
    }

    public Customer updateCustomer(Customer customer) {
        return customerRepository.save(customer);
    }

    public Customer getCustomerByUser(User user) {
        return customerRepository.findByUser(user);
    }

    public boolean validateCustomerInfo(Customer customer) {
        // Kiểm tra thông tin khách hàng hợp lệ
        return customer.getFullName() != null && !customer.getFullName().isEmpty() &&
                customer.getPhoneNumber() != null && customer.getPhoneNumber().matches("^[0-9]{10}$");
    }
}