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
import com.hostel.management.repository.*;
import com.hostel.management.model.Payment;
import javax.servlet.http.HttpSession;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Calendar;
import javax.servlet.http.HttpSession;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.hostel.management.model.Invoice;
import com.hostel.management.service.InvoiceService;


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

    @Autowired
    private InvoiceService invoiceService;

    // Thêm repository cần thiết
    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private UtilityReadingRepository utilityReadingRepository;

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
        return "redirect:/admin/rooms/manage";
    }

    @GetMapping("/bookings/manage")
    public String manageBookings(HttpSession session, Model model) {
        // Kiểm tra quyền admin
        User user = (User) session.getAttribute("user");
        if (user == null || !"ADMIN".equals(user.getRole())) {
            return "redirect:/login";
        }
        return "admin/bookings/manage";
    }

    @GetMapping("/customers/manage")
    public String manageCustomers(HttpSession session, Model model) {
        // Kiểm tra quyền admin
        User user = (User) session.getAttribute("user");
        if (user == null || !"ADMIN".equals(user.getRole())) {
            return "redirect:/login";
        }
        return "admin/customers/manage";
    }

    @GetMapping("/utility-readings")
    public String showUtilityReadingsForm(@RequestParam(required = false) Integer month,
                                          @RequestParam(required = false) Integer year,
                                          HttpSession session, Model model) {
        // Kiểm tra quyền admin
        User user = (User) session.getAttribute("user");
        if (user == null || !"ADMIN".equals(user.getRole())) {
            return "redirect:/login";
        }

        // Nếu không có tháng/năm, lấy tháng/năm hiện tại
        Calendar cal = Calendar.getInstance();
        if (month == null) {
            month = cal.get(Calendar.MONTH) + 1; // Calendar.MONTH bắt đầu từ 0
        }
        if (year == null) {
            year = cal.get(Calendar.YEAR);
        }

        // Lấy danh sách phòng chưa nhập chỉ số trong tháng đó
        List<Room> availableRooms = utilityService.getRoomsWithoutReadingInMonth(month, year);

        model.addAttribute("rooms", availableRooms);
        model.addAttribute("selectedMonth", month);
        model.addAttribute("selectedYear", year);

        return "admin/utility-readings";
    }
//    lấy chỉ số cũ
    @GetMapping("/utility-readings/previous/{roomId}")
    @ResponseBody
    public Map<String, Object> getPreviousReadings(@PathVariable int roomId,
                                                   @RequestParam int month,
                                                   @RequestParam int year) {
        Map<String, Object> response = new HashMap<>();

        try {
            UtilityReading lastReading = utilityService.getLastReadingBeforeMonth(roomId, month, year);

            if (lastReading != null) {
                response.put("electric", lastReading.getElectricReading());
                response.put("water", lastReading.getWaterReading());
                response.put("readingDate", lastReading.getReadingDate());
            } else {
                // Nếu không có chỉ số cũ, trả về 0
                response.put("electric", 0);
                response.put("water", 0);
                response.put("readingDate", null);
            }
        } catch (Exception e) {
            response.put("error", e.getMessage());
        }

        return response;
    }

    @PostMapping("/utility-readings/save")
    public String saveUtilityReadings(@RequestParam int roomId,
                                      @RequestParam int month,
                                      @RequestParam int year,
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

            // Kiểm tra xem đã có chỉ số trong tháng này chưa
            if (utilityService.hasReadingInMonth(roomId, month, year)) {
                throw new RuntimeException("Phòng này đã có chỉ số trong tháng " + month + "/" + year);
            }

            // Lấy chỉ số cũ từ tháng trước
            UtilityReading lastReading = utilityService.getLastReadingBeforeMonth(roomId, month, year);
            double previousElectric = lastReading != null ? lastReading.getElectricReading() : 0;
            double previousWater = lastReading != null ? lastReading.getWaterReading() : 0;

            // Sử dụng thời điểm hiện tại làm ngày ghi chỉ số
            Date readingDate = new Date();

            // Lưu chỉ số mới và tính toán hóa đơn
            UtilityReading newReading = utilityService.saveReadingForMonth(roomId, readingDate, electricReading, waterReading, month, year);

            // Lấy hóa đơn mới được tạo
            Invoice invoice = invoiceService.getInvoiceByUtilityReading(newReading);

            // Tính lượng tiêu thụ
            double electricUsage = electricReading - previousElectric;
            double waterUsage = waterReading - previousWater;

            // Lấy chi tiết tính toán điện nước theo bậc thang
            Map<String, Double> electricDetails = utilityService.getElectricCalculationDetails(electricUsage);
            Map<String, Double> waterDetails = utilityService.getWaterCalculationDetails(waterUsage);

            // Chuyển dữ liệu vào model để hiển thị trang thành công
            model.addAttribute("room", room);
            model.addAttribute("readingDate", readingDate);
            model.addAttribute("month", month);
            model.addAttribute("year", year);
            model.addAttribute("previousElectric", previousElectric);
            model.addAttribute("previousWater", previousWater);
            model.addAttribute("electricReading", electricReading);
            model.addAttribute("waterReading", waterReading);
            model.addAttribute("electricUsage", electricUsage);
            model.addAttribute("waterUsage", waterUsage);
            model.addAttribute("electricTotal", newReading.getElectricTotal());
            model.addAttribute("waterTotal", newReading.getWaterTotal());
            model.addAttribute("electricDetails", electricDetails);
            model.addAttribute("waterDetails", waterDetails);
            model.addAttribute("invoiceId", invoice.getId());

            return "admin/utility-readings-success";
        } catch (Exception e) {
            // Xử lý lỗi
            return "redirect:/admin/utility-readings?month=" + month + "&year=" + year + "&error=" + e.getMessage();
        }
    }
}