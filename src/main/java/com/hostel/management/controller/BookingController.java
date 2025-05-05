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
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.util.Date;
import java.util.Calendar;

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

        // Khởi tạo ngày bắt đầu là ngày mai
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, 1);
        booking.setStartDate(calendar.getTime());

        // Lấy thông tin khách hàng nếu đã có
        Customer customer = customerService.getCustomerByUser(user);

        // Nếu không có thông tin khách hàng, tạo đối tượng mới
        if (customer == null) {
            customer = new Customer();
            // Không gọi setEmail vì Customer không có trường này
        }

        model.addAttribute("booking", booking);
        model.addAttribute("room", room);
        model.addAttribute("customer", customer);

        // Thêm các thuộc tính bổ sung cho form
        model.addAttribute("duration", 6); // Mặc định 6 tháng
        model.addAttribute("numTenants", 2); // Mặc định 2 người
        model.addAttribute("vehicle", "none"); // Mặc định không có phương tiện
        model.addAttribute("email", user.getEmail()); // Email từ user

        return "booking/bookingForm";
    }


    @PostMapping("/booking/create")
    public String createBooking(
            @RequestParam(value = "roomId.id", required = true) int roomId,
            @RequestParam(value = "startDate", required = true) String startDateStr, // Nhận dưới dạng String
            @RequestParam(value = "fullName", required = true) String fullName,
            @RequestParam(value = "phoneNumber", required = true) String phoneNumber,
            @RequestParam(value = "email", required = false) String email,
            @RequestParam(value = "identityCard", required = false) String identityCard,
            @RequestParam(value = "address", required = false) String address,
            @RequestParam(value = "duration", defaultValue = "6") int duration,
            @RequestParam(value = "numTenants", defaultValue = "2") int numTenants,
            @RequestParam(value = "vehicle", defaultValue = "none") String vehicle,
            @RequestParam(value = "notes", required = false) String notes,
            HttpSession session,
            Model model) {

        System.out.println("Bắt đầu xử lý đặt phòng với ID phòng: " + roomId);
        System.out.println("Ngày nhận phòng: " + startDateStr);

        try {
            // Chuyển chuỗi ngày thành đối tượng Date
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            Date startDate = dateFormat.parse(startDateStr);

            // Lấy user từ session
            User user = (User) session.getAttribute("user");
            if (user == null) {
                return "redirect:/login";
            }

            // Lấy thông tin phòng
            Room room = roomService.getRoomById(roomId);
            if (room == null) {
                throw new RuntimeException("Không tìm thấy thông tin phòng");
            }

            // Lấy hoặc tạo mới customer
            Customer customer = customerService.getCustomerByUser(user);
            if (customer == null) {
                customer = new Customer();
                customer.setUser(user);
            }

            // Cập nhật thông tin customer
            customer.setFullName(fullName);
            customer.setPhoneNumber(phoneNumber);

            // Lưu hoặc cập nhật customer
            customer = customerService.createCustomer(customer);

            // Tạo đối tượng Booking mới
            Booking booking = new Booking();
            booking.setRoomId(room);
            booking.setCustomerId(customer);
            booking.setStartDate(startDate);
            booking.setBookingDate(new Date());
            booking.setStatus("pending");

            // Tính tiền đặt cọc
            booking.setDeposit(room.getPrice() * 0.3f); // 30% giá phòng

            // Lưu thông tin đặt phòng
            Booking savedBooking = bookingService.createBooking(booking);

            // Lưu ID đặt phòng vào session
            session.setAttribute("bookingId", savedBooking.getId());

            return "redirect:/booking/confirm/" + savedBooking.getId();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Lỗi khi xử lý đặt phòng: " + e.getMessage());

            model.addAttribute("error", "Có lỗi xảy ra khi xử lý form: " + e.getMessage());

            // Lấy lại thông tin phòng
            Room room = roomService.getRoomById(roomId);
            model.addAttribute("room", room);

            // Trả lại các giá trị đã nhập vào form
            Customer customer = new Customer();
            customer.setFullName(fullName);
            customer.setPhoneNumber(phoneNumber);
            model.addAttribute("customer", customer);
            model.addAttribute("email", email);

            // Trả lại các giá trị khác
            model.addAttribute("duration", duration);
            model.addAttribute("numTenants", numTenants);
            model.addAttribute("vehicle", vehicle);
            model.addAttribute("notes", notes);

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