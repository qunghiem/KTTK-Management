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
import com.hostel.management.dto.RoomRevenue;
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

//    @GetMapping("/reports")
//    public String showReports(HttpSession session, Model model) {
//        // Kiểm tra quyền admin
//        User user = (User) session.getAttribute("user");
//        if (user == null || !"ADMIN".equals(user.getRole())) {
//            return "redirect:/login";
//        }
//
//        // Thêm dữ liệu cho báo cáo
//
//        return "admin/reports";
//    }

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

    @GetMapping("/reports")
    public String showReports(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            HttpSession session,
            Model model) {

        // Kiểm tra quyền admin
        User user = (User) session.getAttribute("user");
        if (user == null || !"ADMIN".equals(user.getRole())) {
            return "redirect:/login";
        }

        // Thiết lập giá trị mặc định là tháng hiện tại
        if (startDate == null) {
            // Lấy ngày đầu tiên của tháng hiện tại
            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.DAY_OF_MONTH, 1);
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            startDate = dateFormat.format(cal.getTime());
        }

        if (endDate == null) {
            // Lấy ngày hiện tại làm ngày kết thúc
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            endDate = dateFormat.format(new Date());
        }
        // Thêm debug info để xem thông tin đầu vào
        System.out.println("Start Date: " + startDate);
        System.out.println("End Date: " + endDate);

        try {
            // Chuyển chuỗi ngày thành đối tượng Date
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            Date startDateObj = dateFormat.parse(startDate);
            Date endDateObj = dateFormat.parse(endDate);

            // In thông tin sau khi parse để kiểm tra
            System.out.println("Parsed Start Date: " + startDateObj);
            System.out.println("Parsed End Date: " + endDateObj);

            // Đảm bảo endDate không sớm hơn startDate
            if (endDateObj.before(startDateObj)) {
                model.addAttribute("error", "Ngày kết thúc không thể sớm hơn ngày bắt đầu");
                return "admin/reports";
            }

            // Tính tổng doanh thu và doanh thu theo phòng từ database
            double totalRevenue = calculateTotalRevenue(startDateObj, endDateObj);
            List<RoomRevenue> roomRevenues = calculateRoomRevenues(startDateObj, endDateObj);

//            if (roomRevenues.isEmpty() && totalRevenue > 0) {
//                // Nếu không có chi tiết nhưng có tổng, đặt tổng về 0
//                totalRevenue = 0;
//            } else if (!roomRevenues.isEmpty() && totalRevenue == 0) {
//                // Nếu có chi tiết nhưng tổng là 0, tính lại tổng từ chi tiết
//                totalRevenue = roomRevenues.stream()
//                        .mapToDouble(RoomRevenue::getAmount)
//                        .sum();
//            }
            // Tính tổng doanh thu từ danh sách chi tiết doanh thu theo phòng
            double totalFromRooms = roomRevenues.stream()
                    .mapToDouble(RoomRevenue::getAmount)
                    .sum();

// Luôn sử dụng giá trị được tính từ chi tiết
            totalRevenue = totalFromRooms;

            // Truyền dữ liệu vào model
            model.addAttribute("startDate", startDate);
            model.addAttribute("endDate", endDate);
            model.addAttribute("totalRevenue", totalRevenue);
            model.addAttribute("roomRevenues", roomRevenues);

        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "Có lỗi xảy ra khi tính toán doanh thu: " + e.getMessage());
        }

        // Trả về trang thống kê
        return "admin/reports";
    }

    // Phương thức tính tổng doanh thu
    private double calculateTotalRevenue(Date startDate, Date endDate) {
        try {
            // Điều chỉnh endDate để bao gồm cả ngày cuối cùng
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(endDate);
            calendar.add(Calendar.DAY_OF_MONTH, 1);
            calendar.add(Calendar.MILLISECOND, -1);
            Date adjustedEndDate = calendar.getTime();

            // Lấy danh sách thanh toán trong khoảng thời gian
            List<Payment> payments = paymentRepository.findByPaymentDateBetween(startDate, adjustedEndDate);

            // Kiểm tra nếu không có thanh toán nào, trả về 0
            if (payments == null || payments.isEmpty()) {
                System.out.println("No payments found in the date range");
                return 0.0;
            }

            // Tính tổng từ danh sách thanh toán
            double total = payments.stream()
                    .mapToDouble(Payment::getAmount)
                    .sum();

            System.out.println("Total revenue: " + total);
            return total;
        } catch (Exception e) {
            System.err.println("Error calculating total revenue: " + e.getMessage());
            e.printStackTrace();
            return 0.0;
        }
    }

    // Phương thức tính doanh thu theo từng phòng
    private List<RoomRevenue> calculateRoomRevenues(Date startDate, Date endDate) {
        // Danh sách lưu trữ kết quả
        List<RoomRevenue> roomRevenues = new ArrayList<>();

        // Lấy tất cả phòng
        List<Room> rooms = roomRepository.findAll();

        for (Room room : rooms) {
            // Tính tổng doanh thu của phòng
            double roomRevenue = calculateRoomRevenue(room, startDate, endDate);

            // Thêm vào danh sách kết quả
            if (roomRevenue > 0) {
                roomRevenues.add(new RoomRevenue(room, roomRevenue));
            }
        }

        // Sắp xếp theo doanh thu giảm dần
        roomRevenues.sort(Comparator.comparing(RoomRevenue::getAmount).reversed());

        return roomRevenues;
    }

    // Phương thức tính doanh thu cho một phòng cụ thể
    private double calculateRoomRevenue(Room room, Date startDate, Date endDate) {
        try {
            // Điều chỉnh endDate để bao gồm cả ngày cuối cùng
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(endDate);
            calendar.add(Calendar.DAY_OF_MONTH, 1);
            calendar.add(Calendar.MILLISECOND, -1);
            Date adjustedEndDate = calendar.getTime();

            // Lấy tất cả booking của phòng trong khoảng thời gian
            List<Booking> bookings = bookingRepository.findByRoomIdAndStartDateBetween(room, startDate, adjustedEndDate);
            System.out.println("Room " + room.getId() + " - Found " + bookings.size() + " bookings");

            // Tính tổng doanh thu từ các booking
            double bookingRevenue = 0.0;
            for (Booking booking : bookings) {
                // Tìm các khoản thanh toán liên quan đến booking này
                Payment payment = paymentRepository.findByBookingId(booking.getId());

                // Cộng dồn vào tổng doanh thu nếu có thanh toán
                if (payment != null) {
                    bookingRevenue += payment.getAmount();
                    System.out.println("- Booking " + booking.getId() + ": " + payment.getAmount());
                }
            }

            // Lấy tất cả hóa đơn tiện ích của phòng trong khoảng thời gian
            List<UtilityReading> utilityReadings = utilityReadingRepository.findByRoomAndReadingDateBetween(room, startDate, adjustedEndDate);
            System.out.println("Room " + room.getId() + " - Found " + utilityReadings.size() + " utility readings");

            // Tính tổng tiền tiện ích
            double utilityRevenue = utilityReadings.stream()
                    .mapToDouble(ur -> ur.getElectricTotal() + ur.getWaterTotal())
                    .sum();

            if (utilityRevenue > 0) {
                System.out.println("- Utility revenue: " + utilityRevenue);
            }

            // Tổng doanh thu của phòng = doanh thu từ booking + doanh thu từ tiện ích
            double totalRevenue = bookingRevenue + utilityRevenue;
            if (totalRevenue > 0) {
                System.out.println("Room " + room.getId() + " - Total revenue: " + totalRevenue);
            }
            return totalRevenue;
        } catch (Exception e) {
            System.err.println("Error calculating room revenue for room " + room.getId() + ": " + e.getMessage());
            e.printStackTrace();
            return 0.0;
        }
    }

    @GetMapping("/rooms/revenue/{roomId}")
    public String showRoomRevenueDetail(@PathVariable int roomId,
                                        @RequestParam(required = false) String startDate,
                                        @RequestParam(required = false) String endDate,
                                        HttpSession session,
                                        Model model) {
        // Kiểm tra quyền admin
        User user = (User) session.getAttribute("user");
        if (user == null || !"ADMIN".equals(user.getRole())) {
            return "redirect:/login";
        }

        // Lấy thông tin phòng
        Room room = roomService.getRoomById(roomId);
        if (room == null) {
            return "redirect:/admin/reports?error=room_not_found";
        }

        try {
            // Xử lý startDate và endDate tương tự như trong showReports
            if (startDate == null) {
                Calendar cal = Calendar.getInstance();
                cal.set(Calendar.DAY_OF_MONTH, 1);
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                startDate = dateFormat.format(cal.getTime());
            }

            if (endDate == null) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                endDate = dateFormat.format(new Date());
            }

            // Chuyển chuỗi ngày thành đối tượng Date
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            Date startDateObj = dateFormat.parse(startDate);
            Date endDateObj = dateFormat.parse(endDate);

            // Lấy chi tiết doanh thu của phòng
            List<Map<String, Object>> revenueDetails = getRoomRevenueDetails(room, startDateObj, endDateObj);

            // Tính tổng doanh thu
            double totalRevenue = revenueDetails.stream()
                    .mapToDouble(detail -> (double) detail.get("totalAmount"))
                    .sum();

            // Truyền dữ liệu vào model
            model.addAttribute("room", room);
            model.addAttribute("startDate", startDate);
            model.addAttribute("endDate", endDate);
            model.addAttribute("revenueDetails", revenueDetails);
            model.addAttribute("totalRevenue", totalRevenue);

            return "admin/room-revenue-detail";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "Có lỗi xảy ra khi tải dữ liệu: " + e.getMessage());
            return "redirect:/admin/reports";
        }
    }

    // Phương thức lấy chi tiết doanh thu theo phòng và thời gian
    private List<Map<String, Object>> getRoomRevenueDetails(Room room, Date startDate, Date endDate) {
        List<Map<String, Object>> results = new ArrayList<>();

        // Điều chỉnh endDate
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(endDate);
        calendar.add(Calendar.DAY_OF_MONTH, 1);
        calendar.add(Calendar.MILLISECOND, -1);
        Date adjustedEndDate = calendar.getTime();

        try {
            // Lấy danh sách booking
            List<Booking> bookings = bookingRepository.findByRoomIdAndStartDateBetween(room, startDate, adjustedEndDate);

            // Lấy danh sách tiện ích
            List<UtilityReading> utilities = utilityReadingRepository.findByRoomAndReadingDateBetween(room, startDate, adjustedEndDate);

            // Tạo map để lưu trữ dữ liệu theo tháng/năm
            Map<String, Map<String, Object>> monthlyData = new HashMap<>();

            // Xử lý dữ liệu booking
            for (Booking booking : bookings) {
                Payment payment = paymentRepository.findByBookingId(booking.getId());
                if (payment != null) {
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(booking.getStartDate());
                    String monthYear = (cal.get(Calendar.MONTH) + 1) + "/" + cal.get(Calendar.YEAR);

                    // Tạo hoặc cập nhật dữ liệu tháng
                    if (!monthlyData.containsKey(monthYear)) {
                        Map<String, Object> detail = new HashMap<>();
                        detail.put("monthYear", monthYear);
                        detail.put("tenantName", booking.getCustomerId().getFullName());
                        detail.put("roomAmount", payment.getAmount());
                        detail.put("utilityAmount", 0.0);
                        detail.put("totalAmount", payment.getAmount());
                        monthlyData.put(monthYear, detail);
                    } else {
                        Map<String, Object> detail = monthlyData.get(monthYear);
                        double currentRoomAmount = (double) detail.get("roomAmount");
                        detail.put("roomAmount", currentRoomAmount + payment.getAmount());

                        double currentTotal = (double) detail.get("totalAmount");
                        detail.put("totalAmount", currentTotal + payment.getAmount());
                    }
                }
            }

            // Xử lý dữ liệu tiện ích
            for (UtilityReading utility : utilities) {
                Calendar cal = Calendar.getInstance();
                cal.setTime(utility.getReadingDate());
                String monthYear = (cal.get(Calendar.MONTH) + 1) + "/" + cal.get(Calendar.YEAR);

                double utilityAmount = utility.getElectricTotal() + utility.getWaterTotal();

                // Cập nhật dữ liệu tháng
                if (!monthlyData.containsKey(monthYear)) {
                    Map<String, Object> detail = new HashMap<>();
                    detail.put("monthYear", monthYear);
                    detail.put("tenantName", ""); // Không có tên khách hàng từ tiện ích
                    detail.put("roomAmount", 0.0);
                    detail.put("utilityAmount", utilityAmount);
                    detail.put("totalAmount", utilityAmount);
                    monthlyData.put(monthYear, detail);
                } else {
                    Map<String, Object> detail = monthlyData.get(monthYear);
                    double currentUtilityAmount = (double) detail.get("utilityAmount");
                    detail.put("utilityAmount", currentUtilityAmount + utilityAmount);

                    double currentTotal = (double) detail.get("totalAmount");
                    detail.put("totalAmount", currentTotal + utilityAmount);
                }
            }

            // Chuyển map thành list và sắp xếp theo tháng/năm
            results = new ArrayList<>(monthlyData.values());
            results.sort((d1, d2) -> {
                String[] parts1 = ((String) d1.get("monthYear")).split("/");
                String[] parts2 = ((String) d2.get("monthYear")).split("/");

                int month1 = Integer.parseInt(parts1[0]);
                int year1 = Integer.parseInt(parts1[1]);
                int month2 = Integer.parseInt(parts2[0]);
                int year2 = Integer.parseInt(parts2[1]);

                if (year1 != year2) {
                    return year1 - year2;
                }
                return month1 - month2;
            });

        } catch (Exception e) {
            e.printStackTrace();
        }

        return results;
    }

}