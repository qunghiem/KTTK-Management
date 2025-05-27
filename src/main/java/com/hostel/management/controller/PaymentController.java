package com.hostel.management.controller;

import com.hostel.management.model.Booking;
import com.hostel.management.model.Room;
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

    // Chỉ hiển thị những method cần thay đổi trong PaymentController

    @GetMapping("/payment/{bookingId}")
    public String showPaymentForm(@PathVariable int bookingId, Model model, HttpSession session) {
        // Kiểm tra đăng nhập
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }

        // THAY ĐỔI: Tạo Booking object để tìm kiếm
        Booking searchBooking = new Booking();
        searchBooking.setId(bookingId);
        Booking booking = bookingService.getBooking(searchBooking);

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
        payment.setBooking(booking);  // Sử dụng tham chiếu trực tiếp

        model.addAttribute("booking", booking);
        model.addAttribute("payment", payment);
        model.addAttribute("paymentMethods", paymentService.getPaymentMethods());

        return "payment/payment";
    }

    @GetMapping("/booking/success/{bookingId}")
    public String showBookingSuccess(@PathVariable int bookingId, Model model, HttpSession session) {
        // Kiểm tra đăng nhập
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }

        // THAY ĐỔI: Tạo Booking object để tìm kiếm
        Booking searchBooking = new Booking();
        searchBooking.setId(bookingId);
        Booking booking = bookingService.getBooking(searchBooking);

        if (booking == null) {
            return "redirect:/customer/bookings?error=booking_not_found";
        }

        // Lấy thông tin thanh toán từ session
        Payment payment = (Payment) session.getAttribute("payment");

        // Nếu không có trong session, tìm kiếm theo booking
        if (payment == null) {
            payment = paymentService.getPaymentByBooking(booking);
        }

        model.addAttribute("booking", booking);
        model.addAttribute("payment", payment);
        model.addAttribute("bookingCode", "BK" + String.format("%06d", booking.getId()));

        return "booking/bookingSuccess";
    }

    @PostMapping("/payment/confirm/{bookingId}")
    public String confirmPayment(@PathVariable int bookingId, HttpSession session) {
        try {
            // THAY ĐỔI: Tạo Booking object để tìm kiếm
            Booking searchBooking = new Booking();
            searchBooking.setId(bookingId);
            Booking booking = bookingService.getBooking(searchBooking);

            if (booking == null) {
                return "redirect:/customer/bookings?error=booking_not_found";
            }

            // Cập nhật trạng thái booking thành "confirmed"
            booking.setStatus("confirmed");

            // Cập nhật trạng thái phòng thành "booked"
            Room room = booking.getRoomId();
            room.setStatus("booked");

            // Lưu cập nhật booking (bao gồm cả đối tượng Room đã cập nhật)
            bookingService.updateBooking(booking);

            // Tạo một Payment mới
            Payment payment = new Payment();
            payment.setAmount(booking.getDeposit());
            payment.setCustomerId(booking.getCustomerId());
            payment.setMethod("bank_transfer"); // Hoặc lấy từ session
            payment.setPaymentDate(new Date());
            payment.setTransactionCode("TX" + System.currentTimeMillis()); // Tạo mã giao dịch
            payment.setBooking(booking); // Gán booking object trực tiếp

            // Lưu thông tin thanh toán
            Payment processedPayment = paymentService.processPayment(payment);

            // Lưu payment ID vào session để sử dụng ở trang success
            session.setAttribute("payment", processedPayment);

            // Chuyển đến trang thành công
            return "redirect:/booking/success/" + bookingId;
        } catch (Exception e) {
            return "redirect:/payment/" + bookingId + "?error=" + e.getMessage();
        }
    }
}