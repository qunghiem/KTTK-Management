package com.hostel.management.controller;

import com.hostel.management.model.Invoice;
import com.hostel.management.model.Room;
import com.hostel.management.model.User;
import com.hostel.management.model.UtilityReading;
import com.hostel.management.repository.InvoiceRepository;
import com.hostel.management.service.InvoiceService;
import com.hostel.management.service.RoomService;
import com.hostel.management.service.UtilityService;
import com.hostel.management.repository.UtilityReadingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/invoices")
public class InvoiceController {

    @Autowired
    private InvoiceService invoiceService;

    @Autowired
    private RoomService roomService;

    @Autowired
    private UtilityService utilityService;

    @Autowired
    private UtilityReadingRepository utilityReadingRepository;

    @Autowired
    private InvoiceRepository invoiceRepository;

    // Hiển thị danh sách hóa đơn với bộ lọc
    @GetMapping
    public String listInvoices(
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Boolean paidStatus,
            @RequestParam(required = false) Integer roomId,
            HttpSession session,
            Model model) {

        // Kiểm tra quyền admin
        User user = (User) session.getAttribute("user");
        if (user == null || !"ADMIN".equals(user.getRole())) {
            return "redirect:/login";
        }

        // Lấy tất cả hóa đơn
        List<Invoice> invoices = invoiceService.getAllInvoices();

        // Áp dụng bộ lọc nếu có
        if (month != null) {
            invoices = invoices.stream()
                    .filter(i -> i.getMonth() == month)
                    .collect(Collectors.toList());
        }

        if (year != null) {
            invoices = invoices.stream()
                    .filter(i -> i.getYear() == year)
                    .collect(Collectors.toList());
        }

        if (paidStatus != null) {
            invoices = invoices.stream()
                    .filter(i -> i.isPaidStatus() == paidStatus)
                    .collect(Collectors.toList());
        }

        if (roomId != null) {
            invoices = invoices.stream()
                    .filter(i -> i.getRoom().getId() == roomId)
                    .collect(Collectors.toList());
        }

        // Lấy danh sách phòng để hiển thị trong form lọc
        List<Room> rooms = roomService.getAllRooms();

        // Lấy năm hiện tại để hiển thị trong dropdown năm
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);

        model.addAttribute("invoices", invoices);
        model.addAttribute("rooms", rooms);
        model.addAttribute("currentYear", currentYear);
        model.addAttribute("selectedMonth", month);
        model.addAttribute("selectedYear", year);
        model.addAttribute("selectedPaidStatus", paidStatus);
        model.addAttribute("selectedRoomId", roomId);

        return "admin/invoices/list";
    }

    // Hiển thị chi tiết hóa đơn
    @GetMapping("/{id}")
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

        // Lấy thông tin chỉ số điện nước để hiển thị chi tiết
        UtilityReading utilityReading = null;
        if (invoice.getUtilityReadingId() > 0) {
            utilityReading = utilityReadingRepository.findById(invoice.getUtilityReadingId()).orElse(null);
        }

        model.addAttribute("invoice", invoice);
        model.addAttribute("utilityReading", utilityReading);

        return "admin/invoices/detail";
    }

    // Đánh dấu hóa đơn đã thanh toán
    @PostMapping("/{id}/mark-paid")
    public String markAsPaid(@PathVariable int id, HttpSession session) {
        // Kiểm tra quyền admin
        User user = (User) session.getAttribute("user");
        if (user == null || !"ADMIN".equals(user.getRole())) {
            return "redirect:/login";
        }

        Invoice invoice = invoiceService.getInvoiceById(id);
        if (invoice != null && !invoice.isPaidStatus()) {
            invoice.setPaidStatus(true);
            invoice.setPaymentDate(new Date());
            invoiceService.updateInvoice(invoice);
        }

        return "redirect:/admin/invoices/" + id + "?success=marked_paid";
    }

    // Tạo mới hóa đơn (form nhập thủ công)
    @GetMapping("/create")
    public String showCreateForm(HttpSession session, Model model) {
        // Kiểm tra quyền admin
        User user = (User) session.getAttribute("user");
        if (user == null || !"ADMIN".equals(user.getRole())) {
            return "redirect:/login";
        }

        List<Room> rooms = roomService.getAllRooms();
        model.addAttribute("rooms", rooms);
        model.addAttribute("invoice", new Invoice());

        return "admin/invoices/create";
    }

    // Xử lý tạo hóa đơn thủ công
    @PostMapping("/create")
    public String createInvoice(@ModelAttribute Invoice invoice,
                                @RequestParam int roomId,
                                @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date invoiceDate,
                                HttpSession session) {
        // Kiểm tra quyền admin
        User user = (User) session.getAttribute("user");
        if (user == null || !"ADMIN".equals(user.getRole())) {
            return "redirect:/login";
        }

        try {
            Room room = roomService.getRoomById(roomId);
            if (room == null) {
                throw new RuntimeException("Phòng không tồn tại");
            }

            invoice.setRoom(room);
            invoice.setCreateDate(invoiceDate);

            // Thiết lập tháng và năm từ ngày hóa đơn
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(invoiceDate);
            invoice.setMonth(calendar.get(Calendar.MONTH) + 1);
            invoice.setYear(calendar.get(Calendar.YEAR));

            // Tính tổng hóa đơn
            double totalAmount = invoice.getElectricTotal() + invoice.getWaterTotal() + invoice.getRoomCharge();
            invoice.setTotalAmount(totalAmount);

            Invoice savedInvoice = invoiceService.updateInvoice(invoice);

            return "redirect:/admin/invoices/" + savedInvoice.getId() + "?success=created";
        } catch (Exception e) {
            return "redirect:/admin/invoices/create?error=" + e.getMessage();
        }
    }

    // In hóa đơn
    @GetMapping("/{id}/print")
    public String printInvoice(@PathVariable int id, HttpSession session, Model model) {
        // Kiểm tra quyền admin
        User user = (User) session.getAttribute("user");
        if (user == null || !"ADMIN".equals(user.getRole())) {
            return "redirect:/login";
        }

        Invoice invoice = invoiceService.getInvoiceById(id);
        if (invoice == null) {
            return "redirect:/admin/invoices?error=invoice_not_found";
        }

        // Lấy thông tin chỉ số điện nước
        UtilityReading utilityReading = null;
        if (invoice.getUtilityReadingId() > 0) {
            utilityReading = utilityReadingRepository.findById(invoice.getUtilityReadingId()).orElse(null);
        }

        model.addAttribute("invoice", invoice);
        model.addAttribute("utilityReading", utilityReading);

        return "admin/invoices/print";
    }

    // Xóa hóa đơn (chỉ cho phép xóa hóa đơn chưa thanh toán)
    @PostMapping("/{id}/delete")
    public String deleteInvoice(@PathVariable int id, HttpSession session) {
        // Kiểm tra quyền admin
        User user = (User) session.getAttribute("user");
        if (user == null || !"ADMIN".equals(user.getRole())) {
            return "redirect:/login";
        }

        Invoice invoice = invoiceService.getInvoiceById(id);
        if (invoice != null && !invoice.isPaidStatus()) {
            // Đặt lại trạng thái billed cho UtilityReading nếu có
            if (invoice.getUtilityReadingId() > 0) {
                UtilityReading utilityReading = utilityReadingRepository.findById(invoice.getUtilityReadingId()).orElse(null);
                if (utilityReading != null) {
                    utilityReading.setBilled(false);
                    utilityReadingRepository.save(utilityReading);
                }
            }

            // Xóa hóa đơn
            invoiceService.deleteInvoice(id);
            return "redirect:/admin/invoices?success=deleted";
        }

        return "redirect:/admin/invoices?error=cannot_delete_paid_invoice";
    }
}