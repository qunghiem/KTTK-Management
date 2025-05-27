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
    public String getBookingForm(@PathVariable int roomId,
                                 @RequestParam(required = false) String startDate,
                                 @RequestParam(required = false, defaultValue = "6") int duration,
                                 @RequestParam(required = false, defaultValue = "2") int numTenants,
                                 Model model, HttpSession session) {
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

        // Nếu có truyền startDate từ trang chi tiết phòng
        if (startDate != null && !startDate.isEmpty()) {
            try {
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                Date bookingStartDate = dateFormat.parse(startDate);
                booking.setStartDate(bookingStartDate);
            } catch (Exception e) {
                // Nếu format sai thì khởi tạo ngày mặc định
                Calendar calendar = Calendar.getInstance();
                calendar.add(Calendar.DAY_OF_MONTH, 1);
                booking.setStartDate(calendar.getTime());
            }
        } else {
            // Khởi tạo ngày bắt đầu là ngày mai
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.DAY_OF_MONTH, 1);
            booking.setStartDate(calendar.getTime());
        }

        // Thiết lập thông tin bổ sung
        booking.setDuration(duration);
        booking.setNumTenants(numTenants);

        // Lấy thông tin khách hàng nếu đã có
        Customer customer = customerService.getCustomerByUser(user);

        // Nếu không có thông tin khách hàng, tạo đối tượng mới
        if (customer == null) {
            customer = new Customer();
            customer.setUser(user);
        }

        model.addAttribute("booking", booking);
        model.addAttribute("room", room);
        model.addAttribute("customer", customer);
        model.addAttribute("email", user.getEmail()); // Email từ user

        return "booking/bookingForm";
    }

    @PostMapping("/booking/create")
    public String createBooking(@RequestParam(value = "roomId.id", required = true) int roomId,
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

        System.out.println("=== BOOKING CREATE DEBUG ===");
        System.out.println("Room ID: " + roomId);
        System.out.println("Start Date: " + startDateStr);
        System.out.println("Full Name: " + fullName);
        System.out.println("Phone: " + phoneNumber);
        System.out.println("Email: " + email);
        System.out.println("Duration: " + duration);
        System.out.println("NumTenants: " + numTenants);
        System.out.println("Vehicle: " + vehicle);

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

            // Kiểm tra phòng có sẵn không
            if (!room.getStatus().equals("available")) {
                throw new RuntimeException("Phòng này đã được đặt, vui lòng chọn phòng khác");
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
            customer.setEmail(email);
            if (identityCard != null && !identityCard.trim().isEmpty()) {
                customer.setIdentityCard(identityCard);
            }

            // Lưu email và CMND vào session để sử dụng ở trang xác nhận
            session.setAttribute("customerEmail", email);
            session.setAttribute("customerIdentityCard", identityCard);

            try {
                // Lưu hoặc cập nhật customer
                customer = customerService.createCustomer(customer);
            } catch (Exception e) {
                // Nếu số điện thoại đã tồn tại, tìm khách hàng theo số điện thoại
                Customer existingCustomer = customerService.getCustomerByPhoneNumber(phoneNumber);
                if (existingCustomer != null && existingCustomer.getUser().getId() == user.getId()) {
                    // Nếu số điện thoại thuộc về user hiện tại, sử dụng customer này
                    customer = existingCustomer;
                    // Cập nhật thông tin
                    customer.setFullName(fullName);
                    customer.setEmail(email);
                    if (identityCard != null && !identityCard.trim().isEmpty()) {
                        customer.setIdentityCard(identityCard);
                    }
                    customer = customerService.updateCustomer(customer);
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
            if (notes != null && !notes.trim().isEmpty()) {
                booking.setNotes(notes);
            }

            // Tính tiền đặt cọc
            booking.setDeposit(room.getPrice() * 0.3f); // 30% giá phòng

            // Lưu thông tin đặt phòng
            Booking savedBooking = bookingService.createBooking(booking);

            // Lưu ID đặt phòng vào session
            session.setAttribute("bookingId", savedBooking.getId());

            System.out.println("Booking created successfully with ID: " + savedBooking.getId());

            return "redirect:/booking/confirm/" + savedBooking.getId();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Lỗi khi xử lý đặt phòng: " + e.getMessage());

            try {
                // Lấy lại thông tin phòng để hiển thị form
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

                booking.setDuration(duration);
                booking.setNumTenants(numTenants);
                booking.setVehicle(vehicle);
                booking.setNotes(notes);

                model.addAttribute("booking", booking);

                // Tạo đối tượng customer để hiển thị lại thông tin
                Customer customer = new Customer();
                customer.setFullName(fullName);
                customer.setPhoneNumber(phoneNumber);
                customer.setEmail(email);
                customer.setIdentityCard(identityCard);
                model.addAttribute("customer", customer);

                // Thêm thông báo lỗi
                model.addAttribute("error", "Có lỗi xảy ra: " + e.getMessage());

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

        System.out.println("=== CONFIRM BOOKING DEBUG ===");
        System.out.println("Booking ID: " + bookingId);
        System.out.println("Room: " + booking.getRoomId().getRoomNumber());
        System.out.println("Customer: " + booking.getCustomerId().getFullName());
        System.out.println("Start Date: " + booking.getStartDate());
        System.out.println("Duration: " + booking.getDuration());
        System.out.println("NumTenants: " + booking.getNumTenants());
        System.out.println("Vehicle: " + booking.getVehicle());

        // Thêm email và CMND từ session vào model
        model.addAttribute("customerEmail", session.getAttribute("customerEmail"));
        model.addAttribute("customerIdentityCard", session.getAttribute("customerIdentityCard"));

        model.addAttribute("booking", booking);
        return "booking/confirmBooking";
    }

    @GetMapping("/booking/cancel/{bookingId}")
    public String cancelBooking(@PathVariable int bookingId, HttpSession session) {
        // Kiểm tra đăng nhập
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }

        try {
            Booking booking = bookingService.getBookingById(bookingId);
            if (booking != null) {
                bookingService.cancelBooking(booking);
            }
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

        // Thêm email và CMND từ session vào model khi xem chi tiết
        model.addAttribute("customerEmail", session.getAttribute("customerEmail"));
        model.addAttribute("customerIdentityCard", session.getAttribute("customerIdentityCard"));

        model.addAttribute("booking", booking);
        return "booking/bookingDetail";
    }
}