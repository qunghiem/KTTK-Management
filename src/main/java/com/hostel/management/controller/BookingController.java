package com.hostel.management.controller;

import com.hostel.management.model.Booking;
import com.hostel.management.model.Customer;
import com.hostel.management.model.Room;
import com.hostel.management.model.User;
import com.hostel.management.service.BookingService;
import com.hostel.management.service.CustomerService;
import com.hostel.management.service.RoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.util.Date;

@Controller
public class BookingController {
    @Autowired
    private BookingService bookingService;

    @Autowired
    private RoomService roomService;

    @Autowired
    private CustomerService customerService;

    @GetMapping("/booking/form/{roomId}")
    public String getBookingForm(@PathVariable int roomId, Model model, HttpSession session) {
        // Kiểm tra đăng nhập
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login?redirect=/booking/form/" + roomId;
        }

        // Lấy thông tin phòng
        Room room = roomService.getRoomById(roomId);
        if (room == null) {
            return "redirect:/rooms?error=room_not_found";
        }

        // Kiểm tra phòng có sẵn không
        if (!room.getStatus().equals("available")) {
            return "redirect:/rooms?error=room_not_available";
        }

        // Tạo đối tượng Booking mới
        Booking booking = new Booking();
        booking.setRoomId(room);

        // Lấy thông tin khách hàng nếu đã có
        Customer customer = customerService.getCustomerByUser(user);

        model.addAttribute("booking", booking);
        model.addAttribute("room", room);
        model.addAttribute("customer", customer);

        return "booking/bookingForm";
    }

    @PostMapping("/booking/create")
    public String createBooking(@Valid @ModelAttribute("booking") Booking booking,
                                BindingResult result,
                                HttpSession session,
                                Model model) {
        if (result.hasErrors()) {
            model.addAttribute("room", booking.getRoomId());
            return "booking/bookingForm";
        }

        try {
            // Thiết lập ngày đặt phòng là ngày hiện tại
            booking.setBookingDate(new Date());

            // Lưu thông tin đặt phòng
            Booking savedBooking = bookingService.createBooking(booking);

            // Lưu ID đặt phòng vào session để sử dụng ở bước tiếp theo
            session.setAttribute("bookingId", savedBooking.getId());

            return "redirect:/booking/confirm/" + savedBooking.getId();
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("room", booking.getRoomId());
            return "booking/bookingForm";
        }
    }

    @GetMapping("/booking/confirm/{bookingId}")
    public String confirmBookingForm(@PathVariable int bookingId, Model model, HttpSession session) {
        // Kiểm tra đăng nhập
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }

        // Lấy thông tin đặt phòng
        Booking booking = bookingService.getBookingById(bookingId);

        if (booking == null) {
            return "redirect:/rooms?error=booking_not_found";
        }

        // Kiểm tra quyền truy cập - FIX: sử dụng == thay vì .equals() cho so sánh int
        Customer customer = customerService.getCustomerByUser(user);
        if (customer == null || booking.getCustomerId().getId() != customer.getId()) {
            return "redirect:/rooms?error=unauthorized";
        }

        model.addAttribute("booking", booking);
        return "booking/confirmBooking";
    }

    @PostMapping("/booking/confirm/{bookingId}")
    public String confirmBooking(@PathVariable int bookingId,
                                 @RequestParam String paymentMethod,
                                 HttpSession session,
                                 Model model) {
        // Kiểm tra đăng nhập
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }

        try {
            // Xác nhận đặt phòng - FIX: Sử dụng kết quả trả về
            bookingService.confirmBooking(bookingId); // Bỏ biến confirmedBooking không sử dụng

            // Lưu phương thức thanh toán vào session để sử dụng ở màn hình thanh toán
            session.setAttribute("paymentMethod", paymentMethod);

            return "redirect:/payment/" + bookingId;
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("booking", bookingService.getBookingById(bookingId));
            return "booking/confirmBooking";
        }
    }

    @GetMapping("/booking/cancel/{bookingId}")
    public String cancelBooking(@PathVariable int bookingId, HttpSession session) {
        // Kiểm tra đăng nhập
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }

        try {
            bookingService.cancelBooking(bookingId);
            return "redirect:/customer/bookings?success=booking_cancelled";
        } catch (Exception e) {
            return "redirect:/customer/bookings?error=" + e.getMessage();
        }
    }

    @GetMapping("/booking/{id}")
    public String getBookingById(@PathVariable int id, Model model, HttpSession session) {
        // Kiểm tra đăng nhập
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }

        Booking booking = bookingService.getBookingById(id);

        if (booking == null) {
            return "redirect:/customer/bookings?error=booking_not_found";
        }

        model.addAttribute("booking", booking);
        return "booking/bookingDetail";
    }
}