package com.hostel.management.service;

import com.hostel.management.model.Customer;
import com.hostel.management.model.User;
import com.hostel.management.repository.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CustomerService {
    @Autowired
    private CustomerRepository customerRepository;

    public Customer createCustomer(Customer customer) {
        // Kiểm tra số điện thoại đã tồn tại chưa
        if (customerRepository.existsByPhoneNumber(customer.getPhoneNumber())) {
            Customer existingCustomer = customerRepository.findByPhoneNumber(customer.getPhoneNumber());
            // Nếu số điện thoại thuộc về user hiện tại, cập nhật thông tin
            if (existingCustomer.getUser().getId() == customer.getUser().getId()) {
                existingCustomer.setFullName(customer.getFullName());
                existingCustomer.setEmail(customer.getEmail());
                existingCustomer.setIdentityCard(customer.getIdentityCard());
                return customerRepository.save(existingCustomer);
            }
            throw new RuntimeException("Số điện thoại đã được sử dụng");
        }

        return customerRepository.save(customer);
    }

    public Customer updateCustomer(Customer customer) {
        Customer existingCustomer = customerRepository.findById(customer.getId()).orElse(null);
        if (existingCustomer == null) {
            throw new RuntimeException("Không tìm thấy thông tin khách hàng");
        }

        // Kiểm tra số điện thoại nếu thay đổi
        if (!existingCustomer.getPhoneNumber().equals(customer.getPhoneNumber())) {
            if (customerRepository.existsByPhoneNumber(customer.getPhoneNumber())) {
                throw new RuntimeException("Số điện thoại đã được sử dụng");
            }
        }

        // Cập nhật thông tin
        existingCustomer.setFullName(customer.getFullName());
        existingCustomer.setPhoneNumber(customer.getPhoneNumber());
        existingCustomer.setEmail(customer.getEmail());
        existingCustomer.setIdentityCard(customer.getIdentityCard());

        return customerRepository.save(existingCustomer);
    }

    public Customer getCustomerByUser(User user) {
        return customerRepository.findByUser(user);
    }

    public Customer getCustomerByPhoneNumber(String phoneNumber) {
        return customerRepository.findByPhoneNumber(phoneNumber);
    }

    public Customer getCustomerById(int id) {
        return customerRepository.findById(id).orElse(null);
    }

    public List<Customer> getAllCustomers() {
        return customerRepository.findAll();
    }

    public boolean validateCustomer(Customer customer) {
        // Kiểm tra thông tin khách hàng hợp lệ
        return customer.getFullName() != null && !customer.getFullName().isEmpty() &&
                customer.getPhoneNumber() != null && customer.getPhoneNumber().matches("^[0-9]{10}$");
    }

    public void deleteCustomer(Customer customer) {
        customerRepository.delete(customer);
    }
}