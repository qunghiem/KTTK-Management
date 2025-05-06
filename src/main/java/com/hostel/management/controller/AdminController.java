package com.hostel.management.controller;

import com.hostel.management.model.Booking;
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
import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private RoomService roomService;

    @Autowired
    private BookingService bookingService;

    @Autowired
    private CustomerService customerService;

    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        // Kiểm tra xem người dùng đã đăng nhập chưa
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }

        // Kiểm tra xem người dùng có phải là admin không
        if (!"ADMIN".equals(user.getRole())) {
            return "redirect:/home";
        }

        // Truyền dữ liệu cần thiết cho dashboard
        model.addAttribute("user", user);

        // Lấy số liệu tổng quan
        List<Room> allRooms = roomService.getAllRooms();
        List<Room> availableRooms = roomService.getAvailableRooms();

        model.addAttribute("totalRooms", allRooms.size());
        model.addAttribute("availableRooms", availableRooms.size());
        model.addAttribute("occupiedRooms", allRooms.size() - availableRooms.size());

        // Hiển thị 5 đặt phòng mới nhất
        // Giả sử có phương thức getRecentBookings trong BookingService
        // List<Booking> recentBookings = bookingService.getRecentBookings(5);
        // model.addAttribute("recentBookings", recentBookings);

        return "admin/dashboard";
    }

    @GetMapping("/rooms/manage")
    public String manageRooms(HttpSession session, Model model) {
        // Kiểm tra quyền admin
        User user = (User) session.getAttribute("user");
        if (user == null || !"ADMIN".equals(user.getRole())) {
            return "redirect:/login";
        }

        // Lấy danh sách tất cả phòng
        List<Room> allRooms = roomService.getAllRooms();
        model.addAttribute("rooms", allRooms);

        return "admin/rooms/manage";
    }

    @GetMapping("/rooms/add")
    public String showAddRoomForm(HttpSession session, Model model) {
        // Kiểm tra quyền admin
        User user = (User) session.getAttribute("user");
        if (user == null || !"ADMIN".equals(user.getRole())) {
            return "redirect:/login";
        }

        model.addAttribute("room", new Room());
        return "admin/rooms/form";
    }

    @PostMapping("/rooms/save")
    public String saveRoom(@ModelAttribute Room room, HttpSession session) {
        // Kiểm tra quyền admin
        User user = (User) session.getAttribute("user");
        if (user == null || !"ADMIN".equals(user.getRole())) {
            return "redirect:/login";
        }

        // Lưu phòng (giả sử có phương thức saveRoom trong RoomService)
        // roomService.saveRoom(room);

        return "redirect:/admin/rooms/manage";
    }

    @GetMapping("/bookings/manage")
    public String manageBookings(HttpSession session, Model model) {
        // Kiểm tra quyền admin
        User user = (User) session.getAttribute("user");
        if (user == null || !"ADMIN".equals(user.getRole())) {
            return "redirect:/login";
        }

        // Lấy danh sách đặt phòng (giả sử có phương thức getAllBookings)
        // List<Booking> bookings = bookingService.getAllBookings();
        // model.addAttribute("bookings", bookings);

        return "admin/bookings/manage";
    }

    @GetMapping("/customers/manage")
    public String manageCustomers(HttpSession session, Model model) {
        // Kiểm tra quyền admin
        User user = (User) session.getAttribute("user");
        if (user == null || !"ADMIN".equals(user.getRole())) {
            return "redirect:/login";
        }

        // Lấy danh sách khách hàng (giả sử có phương thức getAllCustomers)
        // List<Customer> customers = customerService.getAllCustomers();
        // model.addAttribute("customers", customers);

        return "admin/customers/manage";
    }

    @GetMapping("/reports")
    public String showReports(HttpSession session, Model model) {
        // Kiểm tra quyền admin
        User user = (User) session.getAttribute("user");
        if (user == null || !"ADMIN".equals(user.getRole())) {
            return "redirect:/login";
        }

        // Thêm dữ liệu cho báo cáo

        return "admin/reports";
    }
}