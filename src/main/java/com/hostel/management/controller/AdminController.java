package com.hostel.management.controller;

import com.hostel.management.model.Booking;
import com.hostel.management.model.Room;
import com.hostel.management.model.User;
import com.hostel.management.model.UtilityReading;
import com.hostel.management.service.BookingService;
import com.hostel.management.service.CustomerService;
import com.hostel.management.service.RoomService;
import com.hostel.management.service.UtilityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private RoomService roomService;

    @Autowired
    private BookingService bookingService;

    @Autowired
    private CustomerService customerService;

    @Autowired
    private UtilityService utilityService;

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

    @GetMapping("/utility-readings")
    public String showUtilityReadingsForm(HttpSession session, Model model) {
        // Kiểm tra quyền admin
        User user = (User) session.getAttribute("user");
        if (user == null || !"ADMIN".equals(user.getRole())) {
            return "redirect:/login";
        }

        // Lấy danh sách phòng
        List<Room> rooms = roomService.getAllRooms();
        model.addAttribute("rooms", rooms);

        return "admin/utility-readings";
    }

    @GetMapping("/utility-readings/previous/{roomId}")
    @ResponseBody
    public Map<String, Object> getPreviousReadings(@PathVariable int roomId) {
        Map<String, Object> response = new HashMap<>();

        try {
            UtilityReading lastReading = utilityService.getLastReading(roomId);

            if (lastReading != null) {
                response.put("electric", lastReading.getElectricReading());
                response.put("water", lastReading.getWaterReading());
            } else {
                // Nếu không có chỉ số cũ, trả về 0
                response.put("electric", 0);
                response.put("water", 0);
            }
        } catch (Exception e) {
            // Xử lý lỗi
            response.put("error", e.getMessage());
        }

        return response;
    }

    @PostMapping("/utility-readings/save")
    public String saveUtilityReadings(@RequestParam int roomId,
                                      @RequestParam String readingDate,
                                      @RequestParam double electricReading,
                                      @RequestParam double waterReading,
                                      HttpSession session,
                                      Model model) {
        // Kiểm tra quyền admin
        User user = (User) session.getAttribute("user");
        if (user == null || !"ADMIN".equals(user.getRole())) {
            return "redirect:/login";
        }

        try {
            // Lấy thông tin phòng
            Room room = roomService.getRoomById(roomId);
            if (room == null) {
                throw new RuntimeException("Phòng không tồn tại");
            }

            // Lấy chỉ số cũ từ database
            UtilityReading lastReading = utilityService.getLastReading(roomId);
            double previousElectric = lastReading != null ? lastReading.getElectricReading() : 0;
            double previousWater = lastReading != null ? lastReading.getWaterReading() : 0;

            // Chuyển đổi chuỗi readingDate thành đối tượng Date
            Date readingDateObj = java.sql.Date.valueOf(readingDate);

            // Lưu chỉ số mới và tính toán hóa đơn
            UtilityReading newReading = utilityService.saveReading(roomId, readingDateObj, electricReading, waterReading);

            // Chuyển dữ liệu vào model để hiển thị trang thành công
            model.addAttribute("room", room);
            model.addAttribute("readingDate", readingDateObj);
            model.addAttribute("previousElectric", previousElectric);
            model.addAttribute("previousWater", previousWater);
            model.addAttribute("electricReading", electricReading);
            model.addAttribute("waterReading", waterReading);
            model.addAttribute("electricTotal", newReading.getElectricTotal());
            model.addAttribute("waterTotal", newReading.getWaterTotal());
            model.addAttribute("invoiceId", newReading.getId());

            return "admin/utility-readings-success";
        } catch (Exception e) {
            // Xử lý lỗi
            return "redirect:/admin/utility-readings?error=" + e.getMessage();
        }
    }
}