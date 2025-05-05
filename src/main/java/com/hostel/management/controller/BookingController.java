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
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.text.SimpleDateFormat;
import java.util.Calendar;
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

        // Khởi tạo ngày bắt đầu là ngày mai
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, 1);
        booking.setStartDate(calendar.getTime());

        // Lấy thông tin khách hàng nếu đã có
        Customer customer = customerService.getCustomerByUser(user);

        // Nếu không có thông tin khách hàng, tạo đối tượng mới
        if (customer == null) {
            customer = new Customer();
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
            @RequestParam(value = "startDate", required = true) String startDateStr,
            @RequestParam(value = "fullName", required = true) String fullName,
            @RequestParam(value = "phoneNumber", required = true) String phoneNumber,
            @RequestParam(value = "email", required = true) String email,
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

            try {
                // Lưu hoặc cập nhật customer
                customer = customerService.createCustomer(customer);
            } catch (Exception e) {
                // Nếu số điện thoại đã tồn tại, tìm khách hàng theo số điện thoại
                Customer existingCustomer = customerService.getCustomerByPhoneNumber(phoneNumber);
                if (existingCustomer != null && existingCustomer.getUser().getId() == user.getId()) {
                    // Nếu số điện thoại thuộc về user hiện tại, sử dụng customer này
                    customer = existingCustomer;
                } else {
                    // Nếu số điện thoại thuộc về người khác, báo lỗi
                    throw new RuntimeException("Số điện thoại đã được sử dụng bởi người dùng khác");
                }
            }

            // Tạo đối tượng Booking mới
            Booking booking = new Booking();
            booking.setRoomId(room);
            booking.setCustomerId(customer);
            booking.setStartDate(startDate);
            booking.setBookingDate(new Date());
            booking.setStatus("pending");

            // Thiết lập các thông tin bổ sung
            booking.setDuration(duration);
            booking.setNumTenants(numTenants);
            booking.setVehicle(vehicle);
            booking.setNotes(notes);

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

            try {
                // Lấy lại thông tin phòng
                Room room = roomService.getRoomById(roomId);
                model.addAttribute("room", room);

                // Tạo đối tượng booking mới để tránh lỗi null
                Booking booking = new Booking();
                booking.setRoomId(room);

                // Thiết lập ngày bắt đầu
                try {
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                    Date startDate = dateFormat.parse(startDateStr);
                    booking.setStartDate(startDate);
                } catch (Exception ex) {
                    Calendar cal = Calendar.getInstance();
                    cal.add(Calendar.DAY_OF_MONTH, 1);
                    booking.setStartDate(cal.getTime());
                }

                model.addAttribute("booking", booking);

                // Tạo đối tượng customer để hiển thị lại thông tin
                Customer customer = new Customer();
                customer.setFullName(fullName);
                customer.setPhoneNumber(phoneNumber);
                model.addAttribute("customer", customer);

                // Thêm thông tin khác
                model.addAttribute("email", email);
                model.addAttribute("duration", duration);
                model.addAttribute("numTenants", numTenants);
                model.addAttribute("vehicle", vehicle);
                model.addAttribute("notes", notes);

                // Thêm thông báo lỗi
                model.addAttribute("error", "Có lỗi xảy ra khi xử lý form: " + e.getMessage());

                return "booking/bookingForm";
            } catch (Exception ex) {
                // Nếu có lỗi khi xử lý lỗi, chuyển hướng về trang danh sách phòng
                return "redirect:/rooms?error=" + e.getMessage();
            }
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

        // Kiểm tra quyền truy cập
        Customer customer = customerService.getCustomerByUser(user);
        if (customer == null || booking.getCustomerId().getId() != customer.getId()) {
            return "redirect:/rooms?error=unauthorized";
        }

        // In thông tin để debug
        System.out.println("Đang hiển thị xác nhận đặt phòng với ID: " + bookingId);
        System.out.println("Phòng: " + booking.getRoomId().getRoomNumber());
        System.out.println("Khách hàng: " + booking.getCustomerId().getFullName());
        System.out.println("Ngày nhận phòng: " + booking.getStartDate());
        System.out.println("Duration: " + booking.getDuration());
        System.out.println("NumTenants: " + booking.getNumTenants());
        System.out.println("Vehicle: " + booking.getVehicle());

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
            // Xác nhận đặt phòng
            bookingService.confirmBooking(bookingId);

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