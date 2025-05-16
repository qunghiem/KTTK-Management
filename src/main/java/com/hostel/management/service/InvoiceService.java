package com.hostel.management.service;

import com.hostel.management.model.Invoice;
import com.hostel.management.model.Room;
import com.hostel.management.model.Customer;
import com.hostel.management.model.UtilityReading;
import com.hostel.management.repository.InvoiceRepository;
import com.hostel.management.repository.BookingRepository;
import com.hostel.management.model.Booking;
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
        invoice.setElectricTotal(invoice.getElectricUsage() * ELECTRIC_PRICE);

        invoice.setWaterReading(utilityReading.getWaterReading());
        invoice.setPreviousWaterReading(previousWater);
        invoice.setWaterUsage(utilityReading.getWaterReading() - previousWater);
        invoice.setWaterPrice(WATER_PRICE);
        invoice.setWaterTotal(invoice.getWaterUsage() * WATER_PRICE);

        // Thiết lập tiền phòng
        invoice.setRoomCharge(room.getPrice());

        // Tính tổng tiền
        double totalAmount = invoice.getElectricTotal() + invoice.getWaterTotal() + room.getPrice();
        invoice.setTotalAmount(totalAmount);

        // Thiết lập trạng thái thanh toán mặc định là chưa thanh toán
        invoice.setPaidStatus(false);

        // Thiết lập tháng và năm của hóa đơn
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(utilityReading.getReadingDate());
        invoice.setMonth(calendar.get(Calendar.MONTH) + 1); // Tháng trong Calendar bắt đầu từ 0
        invoice.setYear(calendar.get(Calendar.YEAR));

        // Lưu ID của bản ghi utility reading
        invoice.setUtilityReadingId(utilityReading.getId());

        return invoiceRepository.save(invoice);
    }
}