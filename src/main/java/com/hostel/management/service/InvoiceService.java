package com.hostel.management.service;

import com.hostel.management.model.Invoice;
import com.hostel.management.model.Room;
import com.hostel.management.model.Customer;
import com.hostel.management.model.UtilityReading;
import com.hostel.management.model.Booking;
import com.hostel.management.repository.InvoiceRepository;
import com.hostel.management.repository.BookingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Service
public class InvoiceService {

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Autowired
    private BookingRepository bookingRepository;

    // Hằng số giá điện, nước (nên đưa vào file cấu hình)
    private final double ELECTRIC_PRICE = 3500; // VNĐ/kWh
    private final double WATER_PRICE = 5000; // VNĐ/m³

    /**
     * Tạo hóa đơn từ UtilityReading (phương thức cũ, giữ lại để tương thích)
     */
    public Invoice createInvoiceFromUtilityReading(UtilityReading utilityReading, double previousElectric, double previousWater) {
        Invoice invoice = new Invoice();
        Room room = utilityReading.getRoom();
        invoice.setRoom(room);

        // Lấy thông tin khách hàng từ booking hiện tại của phòng (nếu có)
        List<Booking> activeBookings = bookingRepository.findByRoomIdAndStatus(room, "confirmed");
        if (!activeBookings.isEmpty()) {
            Booking activeBooking = activeBookings.get(0);
            invoice.setCustomer(activeBooking.getCustomerId());
        }

        // Thiết lập ngày tạo
        invoice.setCreateDate(new Date());

        // Lưu chỉ số điện nước
        invoice.setElectricReading(utilityReading.getElectricReading());
        invoice.setPreviousElectricReading(previousElectric);
        invoice.setElectricUsage(utilityReading.getElectricReading() - previousElectric);
        invoice.setElectricPrice(ELECTRIC_PRICE);
        invoice.setElectricTotal(utilityReading.getElectricTotal()); // Sử dụng tổng đã tính từ UtilityReading

        invoice.setWaterReading(utilityReading.getWaterReading());
        invoice.setPreviousWaterReading(previousWater);
        invoice.setWaterUsage(utilityReading.getWaterReading() - previousWater);
        invoice.setWaterPrice(WATER_PRICE);
        invoice.setWaterTotal(utilityReading.getWaterTotal()); // Sử dụng tổng đã tính từ UtilityReading

        // Thiết lập tiền phòng
        invoice.setRoomCharge(room.getPrice());

        // Tính tổng tiền
        double totalAmount = utilityReading.getElectricTotal() + utilityReading.getWaterTotal() + room.getPrice();
        invoice.setTotalAmount(totalAmount);

        // Thiết lập trạng thái thanh toán mặc định là chưa thanh toán
        invoice.setPaidStatus(false);

        // Thiết lập tháng và năm của hóa đơn
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(utilityReading.getReadingDate());
        invoice.setMonth(calendar.get(Calendar.MONTH) + 1); // Tháng trong Calendar bắt đầu từ 0
        invoice.setYear(calendar.get(Calendar.YEAR));

        // Lưu tham chiếu trực tiếp đến utility reading
        invoice.setUtilityReading(utilityReading);

        return invoiceRepository.save(invoice);
    }

    /**
     * Tạo hóa đơn cho tháng cụ thể từ UtilityReading
     */
    public Invoice createInvoiceFromUtilityReadingForMonth(UtilityReading utilityReading, double previousElectric, double previousWater, int month, int year) {
        Invoice invoice = new Invoice();
        Room room = utilityReading.getRoom();
        invoice.setRoom(room);

        // Lấy thông tin khách hàng từ booking hiện tại của phòng (nếu có)
        List<Booking> activeBookings = bookingRepository.findByRoomIdAndStatus(room, "confirmed");
        if (!activeBookings.isEmpty()) {
            Booking activeBooking = activeBookings.get(0);
            invoice.setCustomer(activeBooking.getCustomerId());
        }

        // Thiết lập ngày tạo
        invoice.setCreateDate(new Date());

        // Lưu chỉ số điện nước
        invoice.setElectricReading(utilityReading.getElectricReading());
        invoice.setPreviousElectricReading(previousElectric);
        invoice.setElectricUsage(utilityReading.getElectricReading() - previousElectric);
        invoice.setElectricPrice(ELECTRIC_PRICE);
        invoice.setElectricTotal(utilityReading.getElectricTotal()); // Sử dụng tổng đã tính từ UtilityReading

        invoice.setWaterReading(utilityReading.getWaterReading());
        invoice.setPreviousWaterReading(previousWater);
        invoice.setWaterUsage(utilityReading.getWaterReading() - previousWater);
        invoice.setWaterPrice(WATER_PRICE);
        invoice.setWaterTotal(utilityReading.getWaterTotal()); // Sử dụng tổng đã tính từ UtilityReading

        // Thiết lập tiền phòng
        invoice.setRoomCharge(room.getPrice());

        // Tính tổng tiền
        double totalAmount = utilityReading.getElectricTotal() + utilityReading.getWaterTotal() + room.getPrice();
        invoice.setTotalAmount(totalAmount);

        // Thiết lập trạng thái thanh toán mặc định là chưa thanh toán
        invoice.setPaidStatus(false);

        // Thiết lập tháng và năm của hóa đơn theo tham số được truyền vào
        invoice.setMonth(month);
        invoice.setYear(year);

        // Lưu tham chiếu trực tiếp đến utility reading
        invoice.setUtilityReading(utilityReading);

        return invoiceRepository.save(invoice);
    }

    /**
     * Tạo hóa đơn mới
     */
    public Invoice createInvoice(Invoice invoice) {
        if (invoice.getCreateDate() == null) {
            invoice.setCreateDate(new Date());
        }
        return invoiceRepository.save(invoice);
    }

    /**
     * Cập nhật hóa đơn
     */
    public Invoice updateInvoice(Invoice invoice) {
        Invoice existingInvoice = invoiceRepository.findById(invoice.getId()).orElse(null);
        if (existingInvoice == null) {
            throw new RuntimeException("Không tìm thấy hóa đơn");
        }
        return invoiceRepository.save(invoice);
    }

    /**
     * Lấy hóa đơn theo utility reading
     */
    public Invoice getInvoiceByUtilityReading(UtilityReading utilityReading) {
        return invoiceRepository.findByUtilityReading(utilityReading);
    }

    /**
     * Lấy tất cả hóa đơn của một phòng
     */
    public List<Invoice> getInvoicesByRoom(Room room) {
        return invoiceRepository.findByRoom(room);
    }

    /**
     * Lấy tất cả hóa đơn của một khách hàng
     */
    public List<Invoice> getInvoicesByCustomer(Customer customer) {
        return invoiceRepository.findByCustomer(customer);
    }

    /**
     * Lấy hóa đơn theo tháng và năm
     */
    public List<Invoice> getInvoicesByMonthAndYear(int month, int year) {
        return invoiceRepository.findByMonthAndYear(month, year);
    }

    /**
     * Lấy hóa đơn theo trạng thái thanh toán
     */
    public List<Invoice> getInvoicesByPaidStatus(boolean paidStatus) {
        return invoiceRepository.findByPaidStatus(paidStatus);
    }

    /**
     * Lấy hóa đơn trong khoảng thời gian
     */
    public List<Invoice> getInvoicesBetweenDates(Date startDate, Date endDate) {
        return invoiceRepository.findByCreateDateBetween(startDate, endDate);
    }

    /**
     * Cập nhật trạng thái thanh toán của hóa đơn
     */
    public Invoice markInvoiceAsPaid(Invoice invoice) {
        Invoice existingInvoice = invoiceRepository.findById(invoice.getId()).orElse(null);
        if (existingInvoice != null) {
            existingInvoice.setPaidStatus(true);
            existingInvoice.setPaymentDate(new Date());
            return invoiceRepository.save(existingInvoice);
        }
        return null;
    }

    /**
     * Cập nhật trạng thái chưa thanh toán của hóa đơn
     */
    public Invoice markInvoiceAsUnpaid(Invoice invoice) {
        Invoice existingInvoice = invoiceRepository.findById(invoice.getId()).orElse(null);
        if (existingInvoice != null) {
            existingInvoice.setPaidStatus(false);
            existingInvoice.setPaymentDate(null);
            return invoiceRepository.save(existingInvoice);
        }
        return null;
    }

    /**
     * Lấy hóa đơn theo ID
     */
    public Invoice getInvoiceById(int invoiceId) {
        return invoiceRepository.findById(invoiceId).orElse(null);
    }

    /**
     * Xóa hóa đơn
     */
    public void deleteInvoice(Invoice invoice) {
        invoiceRepository.delete(invoice);
    }

    /**
     * Tính tổng doanh thu từ các hóa đơn đã thanh toán
     */
    public double calculateTotalRevenue() {
        List<Invoice> paidInvoices = getInvoicesByPaidStatus(true);
        return paidInvoices.stream()
                .mapToDouble(Invoice::getTotalAmount)
                .sum();
    }

    /**
     * Tính tổng doanh thu trong khoảng thời gian
     */
    public double calculateRevenueInPeriod(Date startDate, Date endDate) {
        List<Invoice> invoicesInPeriod = getInvoicesBetweenDates(startDate, endDate);
        return invoicesInPeriod.stream()
                .filter(Invoice::isPaidStatus)
                .mapToDouble(Invoice::getTotalAmount)
                .sum();
    }

    /**
     * Đếm số hóa đơn chưa thanh toán
     */
    public long countUnpaidInvoices() {
        return getInvoicesByPaidStatus(false).size();
    }

    /**
     * Tính tổng số tiền chưa thu
     */
    public double calculateTotalUnpaidAmount() {
        List<Invoice> unpaidInvoices = getInvoicesByPaidStatus(false);
        return unpaidInvoices.stream()
                .mapToDouble(Invoice::getTotalAmount)
                .sum();
    }
}