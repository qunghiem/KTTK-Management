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
import com.hostel.management.model.Payment;
import com.hostel.management.model.Invoice;
import com.hostel.management.service.InvoiceService;

import javax.servlet.http.HttpSession;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Calendar;

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
        List<Room> unenteredRooms = utilityService.getRoomsWithoutReadingInMonth(month, year);

        // Lấy danh sách phòng đã nhập chỉ số trong tháng đó
        List<Room> enteredRooms = utilityService.getRoomsWithReadingInMonth(month, year);

        model.addAttribute("unenteredRooms", unenteredRooms);
        model.addAttribute("enteredRooms", enteredRooms);
        model.addAttribute("selectedMonth", month);
        model.addAttribute("selectedYear", year);

        return "admin/utility-readings";
    }
    // Lấy chỉ số cũ
    @GetMapping("/utility-readings/previous/{roomId}")
    @ResponseBody
    public Map<String, Object> getPreviousReadings(@PathVariable int roomId,
                                                   @RequestParam int month,
                                                   @RequestParam int year) {
        Map<String, Object> response = new HashMap<>();

        try {
            Room room = roomService.getRoomById(roomId);
            if (room == null) {
                response.put("error", "Không tìm thấy phòng");
                return response;
            }

            UtilityReading lastReading = utilityService.getLastReadingBeforeMonth(room, month, year);

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
            if (utilityService.hasReadingInMonth(room, month, year)) {
                throw new RuntimeException("Phòng này đã có chỉ số trong tháng " + month + "/" + year);
            }

            // Lấy chỉ số cũ từ tháng trước
            UtilityReading lastReading = utilityService.getLastReadingBeforeMonth(room, month, year);
            double previousElectric = lastReading != null ? lastReading.getElectricReading() : 0;
            double previousWater = lastReading != null ? lastReading.getWaterReading() : 0;

            // Sử dụng thời điểm hiện tại làm ngày ghi chỉ số
            Date readingDate = new Date();

            // Tạo đối tượng UtilityReading
            UtilityReading utilityReading = new UtilityReading();
            utilityReading.setRoom(room);
            utilityReading.setReadingDate(readingDate);
            utilityReading.setElectricReading(electricReading);
            utilityReading.setWaterReading(waterReading);

            // Lưu chỉ số mới và tính toán hóa đơn
            UtilityReading newReading = utilityService.saveReadingForMonth(utilityReading, month, year);

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

    @GetMapping("/invoices")
    public String manageInvoices(HttpSession session, Model model) {
        // Kiểm tra quyền admin
        User user = (User) session.getAttribute("user");
        if (user == null || !"ADMIN".equals(user.getRole())) {
            return "redirect:/login";
        }

        // Lấy danh sách hóa đơn
        List<Invoice> unpaidInvoices = invoiceService.getInvoicesByPaidStatus(false);
        List<Invoice> paidInvoices = invoiceService.getInvoicesByPaidStatus(true);

        model.addAttribute("unpaidInvoices", unpaidInvoices);
        model.addAttribute("paidInvoices", paidInvoices);

        return "admin/invoices/manage";
    }

    @GetMapping("/invoices/{id}")
    public String viewInvoice(@PathVariable int id, HttpSession session, Model model) {
        // Kiểm tra quyền admin
        User user = (User) session.getAttribute("user");
        if (user == null || !"ADMIN".equals(user.getRole())) {
            return "redirect:/login";
        }

        Invoice invoice = invoiceService.getInvoiceById(id);
        if (invoice == null) {
            return "redirect:/admin/invoices?error=invoice_not_found";
        }

        model.addAttribute("invoice", invoice);
        return "admin/invoices/detail";
    }

    @PostMapping("/invoices/{id}/mark-paid")
    public String markInvoiceAsPaid(@PathVariable int id, HttpSession session) {
        // Kiểm tra quyền admin
        User user = (User) session.getAttribute("user");
        if (user == null || !"ADMIN".equals(user.getRole())) {
            return "redirect:/login";
        }

        try {
            Invoice invoice = invoiceService.getInvoiceById(id);
            if (invoice != null) {
                invoiceService.markInvoiceAsPaid(invoice);
            }
        } catch (Exception e) {
            return "redirect:/admin/invoices?error=" + e.getMessage();
        }

        return "redirect:/admin/invoices";
    }

    @PostMapping("/invoices/{id}/mark-unpaid")
    public String markInvoiceAsUnpaid(@PathVariable int id, HttpSession session) {
        // Kiểm tra quyền admin
        User user = (User) session.getAttribute("user");
        if (user == null || !"ADMIN".equals(user.getRole())) {
            return "redirect:/login";
        }

        try {
            Invoice invoice = invoiceService.getInvoiceById(id);
            if (invoice != null) {
                invoiceService.markInvoiceAsUnpaid(invoice);
            }
        } catch (Exception e) {
            return "redirect:/admin/invoices?error=" + e.getMessage();
        }

        return "redirect:/admin/invoices";
    }

    @GetMapping("/reports")
    public String showReports(HttpSession session, Model model) {
        // Kiểm tra quyền admin
        User user = (User) session.getAttribute("user");
        if (user == null || !"ADMIN".equals(user.getRole())) {
            return "redirect:/login";
        }

        // Tính toán các thống kê cơ bản
        double totalRevenue = invoiceService.calculateTotalRevenue();
        long unpaidInvoices = invoiceService.countUnpaidInvoices();
        double unpaidAmount = invoiceService.calculateTotalUnpaidAmount();

        model.addAttribute("totalRevenue", totalRevenue);
        model.addAttribute("unpaidInvoices", unpaidInvoices);
        model.addAttribute("unpaidAmount", unpaidAmount);

        return "admin/reports";
    }

    // Lấy chỉ số hiện tại của phòng đã nhập
    @GetMapping("/utility-readings/current/{roomId}")
    @ResponseBody
    public Map<String, Object> getCurrentReadings(@PathVariable int roomId,
                                                  @RequestParam int month,
                                                  @RequestParam int year) {
        Map<String, Object> response = new HashMap<>();

        try {
            Room room = roomService.getRoomById(roomId);
            if (room == null) {
                response.put("error", "Không tìm thấy phòng");
                return response;
            }

            // Lấy chỉ số hiện tại trong tháng
            UtilityReading currentReading = utilityService.getReadingInMonth(room, month, year);

            if (currentReading != null) {
                response.put("electric", currentReading.getElectricReading());
                response.put("water", currentReading.getWaterReading());
                response.put("readingDate", currentReading.getReadingDate());
                response.put("readingId", currentReading.getId());

                // Lấy chỉ số cũ để hiển thị
                UtilityReading previousReading = utilityService.getLastReadingBeforeMonth(room, month, year);
                response.put("previousElectric", previousReading != null ? previousReading.getElectricReading() : 0);
                response.put("previousWater", previousReading != null ? previousReading.getWaterReading() : 0);
            } else {
                response.put("error", "Không tìm thấy chỉ số cho tháng này");
            }
        } catch (Exception e) {
            response.put("error", e.getMessage());
        }

        return response;
    }

    // Cập nhật chỉ số đã nhập
    @PostMapping("/utility-readings/update")
    public String updateUtilityReadings(@RequestParam int readingId,
                                        @RequestParam int roomId,
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
            // Lấy chỉ số cũ để validate và tính toán
            UtilityReading existingReading = utilityService.getUtilityReadingById(readingId);
            if (existingReading == null) {
                throw new RuntimeException("Không tìm thấy chỉ số điện nước");
            }

            Room room = roomService.getRoomById(roomId);
            if (room == null) {
                throw new RuntimeException("Phòng không tồn tại");
            }

            // Lấy chỉ số trước đó để validate
            UtilityReading lastReading = utilityService.getLastReadingBeforeMonth(room, month, year);
            double previousElectric = lastReading != null ? lastReading.getElectricReading() : 0;
            double previousWater = lastReading != null ? lastReading.getWaterReading() : 0;

            // Validate chỉ số mới
            if (electricReading < previousElectric) {
                throw new RuntimeException("Chỉ số điện mới không thể nhỏ hơn chỉ số cũ (" + previousElectric + ")");
            }
            if (waterReading < previousWater) {
                throw new RuntimeException("Chỉ số nước mới không thể nhỏ hơn chỉ số cũ (" + previousWater + ")");
            }

            // Cập nhật chỉ số
            existingReading.setElectricReading(electricReading);
            existingReading.setWaterReading(waterReading);

            // Tính lại tổng tiền
            double electricUsage = electricReading - previousElectric;
            double waterUsage = waterReading - previousWater;

            Map<String, Double> electricDetails = utilityService.getElectricCalculationDetails(electricUsage);
            Map<String, Double> waterDetails = utilityService.getWaterCalculationDetails(waterUsage);

            existingReading.setElectricTotal(electricDetails.get("Tổng cộng"));
            existingReading.setWaterTotal(waterDetails.get("Tổng cộng"));

            // Lưu chỉ số đã cập nhật
            UtilityReading updatedReading = utilityService.updateUtilityReading(existingReading);

            // Cập nhật lại hóa đơn tương ứng
            invoiceService.updateInvoiceFromUtilityReading(updatedReading, previousElectric, previousWater, month, year);

            // Chuyển dữ liệu để hiển thị trang thành công
            model.addAttribute("room", room);
            model.addAttribute("readingDate", updatedReading.getReadingDate());
            model.addAttribute("month", month);
            model.addAttribute("year", year);
            model.addAttribute("previousElectric", previousElectric);
            model.addAttribute("previousWater", previousWater);
            model.addAttribute("electricReading", electricReading);
            model.addAttribute("waterReading", waterReading);
            model.addAttribute("electricUsage", electricUsage);
            model.addAttribute("waterUsage", waterUsage);
            model.addAttribute("electricTotal", updatedReading.getElectricTotal());
            model.addAttribute("waterTotal", updatedReading.getWaterTotal());
            model.addAttribute("electricDetails", electricDetails);
            model.addAttribute("waterDetails", waterDetails);
            model.addAttribute("isUpdate", true); // Đánh dấu là cập nhật

            return "admin/utility-readings-success";
        } catch (Exception e) {
            return "redirect:/admin/utility-readings?month=" + month + "&year=" + year + "&error=" + e.getMessage();
        }
    }
}