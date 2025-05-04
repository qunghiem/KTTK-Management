package com.hostel.management.controller;

import com.hostel.management.model.Booking;
import com.hostel.management.model.Customer;
import com.hostel.management.model.User;
import com.hostel.management.repository.BookingRepository;
import com.hostel.management.service.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.util.List;

@Controller
public class CustomerController {
    @Autowired
    private CustomerService customerService;

    @Autowired
    private BookingRepository bookingRepository;

    @GetMapping("/customer/profile")
    public String showProfile(HttpSession session, Model model) {
        // Kiểm tra đăng nhập
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }

        // Lấy thông tin khách hàng
        Customer customer = customerService.getCustomerByUser(user);

        if (customer == null) {
            // Nếu chưa có thông tin khách hàng, chuyển đến trang tạo mới
            return "redirect:/customer/create";
        }

        model.addAttribute("customer", customer);
        return "customer/profile";
    }

    @GetMapping("/customer/create")
    public String showCreateCustomerForm(HttpSession session, Model model) {
        // Kiểm tra đăng nhập
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }

        Customer customer = new Customer();
        customer.setUser(user);

        model.addAttribute("customer", customer);
        return "customer/create";
    }

    @PostMapping("/customer/create")
    public String createCustomer(@Valid @ModelAttribute Customer customer,
                                 BindingResult result,
                                 HttpSession session,
                                 Model model) {
        if (result.hasErrors()) {
            return "customer/create";
        }

        // Kiểm tra đăng nhập
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }

        try {
            customer.setUser(user);
            Customer savedCustomer = customerService.createCustomer(customer);
            return "redirect:/customer/profile?success=profile_created";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "customer/create";
        }
    }

    @GetMapping("/customer/update")
    public String showUpdateForm(HttpSession session, Model model) {
        // Kiểm tra đăng nhập
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }

        // Lấy thông tin khách hàng
        Customer customer = customerService.getCustomerByUser(user);

        if (customer == null) {
            return "redirect:/customer/create";
        }

        model.addAttribute("customer", customer);
        return "customer/update";
    }

    @PostMapping("/customer/update")
    public String updateCustomer(@Valid @ModelAttribute Customer customer,
                                 BindingResult result,
                                 HttpSession session,
                                 Model model) {
        if (result.hasErrors()) {
            return "customer/update";
        }

        // Kiểm tra đăng nhập
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }

        try {
            customer.setUser(user);
            Customer updatedCustomer = customerService.updateCustomer(customer);
            return "redirect:/customer/profile?success=profile_updated";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "customer/update";
        }
    }

    @GetMapping("/customer/bookings")
    public String getCustomerBookings(HttpSession session, Model model) {
        // Kiểm tra đăng nhập
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }

        // Lấy thông tin khách hàng
        Customer customer = customerService.getCustomerByUser(user);

        if (customer == null) {
            return "redirect:/customer/create";
        }

        // Lấy danh sách đặt phòng của khách hàng
        List<Booking> bookings = bookingRepository.findByCustomerId(customer);

        model.addAttribute("bookings", bookings);
        return "customer/bookings";
    }
}