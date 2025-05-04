package com.hostel.management.controller;

import com.hostel.management.model.Booking;
import com.hostel.management.model.Customer;
import com.hostel.management.model.Payment;
import com.hostel.management.model.User;
import com.hostel.management.service.BookingService;
import com.hostel.management.service.CustomerService;
import com.hostel.management.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.Date;
import java.util.List;

@Controller
public class PaymentController {
    @Autowired
    private PaymentService paymentService;

    @Autowired
    private BookingService bookingService;

    @Autowired
    private CustomerService customerService;

    @GetMapping("/payment/{bookingId}")
    public String showPaymentForm(@PathVariable int bookingId, Model model, HttpSession session) {
        // Kiểm tra đăng nhập
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }

        // Lấy thông tin đặt phòng
        Booking booking = bookingService.getBookingById(bookingId);

        if (booking == null) {
            return "redirect:/customer/bookings?error=booking_not_found";
        }

        // Lấy phương thức thanh toán từ session
        String paymentMethod = (String) session.getAttribute("paymentMethod");

        // Nếu không có, lấy phương thức thanh toán mặc định
        if (paymentMethod == null) {
            paymentMethod = "bank_transfer";
        }

        // Tạo đối tượng Payment mới
        Payment payment = new Payment();
        payment.setAmount(booking.getDeposit());
        payment.setCustomerId(booking.getCustomerId());
        payment.setMethod(paymentMethod);

        // Tạo mã QR thanh toán nếu cần
        String qrCode = null;
        if ("momo".equals(paymentMethod) || "zalopay".equals(paymentMethod)) {
            qrCode = paymentService.generatePaymentQR(
                    booking.getDeposit(),
                    "Đặt cọc phòng " + booking.getRoomId().getRoomNumber()
            );
        }

        model.addAttribute("booking", booking);
        model.addAttribute("payment", payment);
        model.addAttribute("qrCode", qrCode);
        model.addAttribute("paymentMethods", paymentService.getPaymentMethods());

        return "payment/payment";
    }

    @PostMapping("/payment/process")
    public String processPayment(@ModelAttribute Payment payment,
                                 @RequestParam int bookingId,
                                 HttpSession session,
                                 Model model) {
        // Kiểm tra đăng nhập
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }

        try {
            // Lấy thông tin đặt phòng
            Booking booking = bookingService.getBookingById(bookingId);

            if (booking == null) {
                throw new RuntimeException("Không tìm thấy đơn đặt phòng");
            }

            // Thiết lập thông tin thanh toán
            payment.setPaymentDate(new Date());

            // Xử lý thanh toán
            Payment processedPayment = paymentService.processPayment(payment);

            // Lưu ID thanh toán vào session
            session.setAttribute("paymentId", processedPayment.getId());

            return "redirect:/booking/success/" + bookingId;
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("booking", bookingService.getBookingById(bookingId));
            return "payment/payment";
        }
    }

    @GetMapping("/payment/verify/{transactionCode}")
    @ResponseBody
    public String verifyPayment(@PathVariable String transactionCode) {
        boolean verified = paymentService.verifyPayment(transactionCode);

        if (verified) {
            return "success";
        } else {
            return "invalid";
        }
    }

    @GetMapping("/booking/success/{bookingId}")
    public String showBookingSuccess(@PathVariable int bookingId, Model model, HttpSession session) {
        // Kiểm tra đăng nhập
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }

        // Lấy thông tin đặt phòng
        Booking booking = bookingService.getBookingById(bookingId);

        if (booking == null) {
            return "redirect:/customer/bookings?error=booking_not_found";
        }

        model.addAttribute("booking", booking);
        model.addAttribute("bookingCode", "BK" + String.format("%06d", booking.getId()));

        return "booking/bookingSuccess";
    }
}