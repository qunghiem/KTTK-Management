package com.hostel.management.service;

import com.hostel.management.model.Room;
import com.hostel.management.model.UtilityReading;
import com.hostel.management.model.Invoice;
import com.hostel.management.repository.RoomRepository;
import com.hostel.management.repository.UtilityReadingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.hostel.management.util.UtilityCalculator;  // Thêm import này
import java.util.Map;  // Thêm import này
import java.util.Date;
import java.util.List;

@Service
public class UtilityService {

    @Autowired
    private UtilityReadingRepository utilityReadingRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private InvoiceService invoiceService;

    private final double ELECTRIC_PRICE = 3500; // VNĐ/kWh
    private final double WATER_PRICE = 5000; // VNĐ/m³

    public UtilityReading getLastReading(int roomId) {
        List<UtilityReading> readings = utilityReadingRepository.findLatestByRoomId(roomId);
        UtilityReading lastReading = readings.isEmpty() ? null : readings.get(0);

        System.out.println("Lấy chỉ số cũ cho phòng " + roomId + ":");
        if (lastReading != null) {
            System.out.println("- Ngày đọc: " + lastReading.getReadingDate());
            System.out.println("- Chỉ số điện: " + lastReading.getElectricReading());
            System.out.println("- Chỉ số nước: " + lastReading.getWaterReading());
        } else {
            System.out.println("- Không tìm thấy chỉ số cũ");
        }

        return lastReading;
    }

    @Transactional
    public UtilityReading saveReading(int roomId, Date readingDate, double electricReading, double waterReading) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Phòng không tồn tại"));

        // Lấy chỉ số cũ
        UtilityReading lastReading = getLastReading(roomId);
        double previousElectric = lastReading != null ? lastReading.getElectricReading() : 0;
        double previousWater = lastReading != null ? lastReading.getWaterReading() : 0;

        // Tính lượng tiêu thụ
        double electricUsage = electricReading - previousElectric;
        double waterUsage = waterReading - previousWater;

        // Tính chi tiết
        Map<String, Double> electricDetails = UtilityCalculator.calculateElectricDetails(electricUsage);
        Map<String, Double> waterDetails = UtilityCalculator.calculateWaterDetails(waterUsage);

        // Lấy tổng từ chi tiết (đảm bảo nhất quán)
        double electricTotal = electricDetails.get("Tổng cộng");
        double waterTotal = waterDetails.get("Tổng cộng");

        // Tạo đối tượng UtilityReading mới
        UtilityReading newReading = new UtilityReading();
        newReading.setRoom(room);
        newReading.setReadingDate(readingDate);
        newReading.setElectricReading(electricReading);
        newReading.setWaterReading(waterReading);
        newReading.setElectricTotal(electricTotal); // Sử dụng tổng từ chi tiết
        newReading.setWaterTotal(waterTotal); // Sử dụng tổng từ chi tiết
        newReading.setBilled(false);

        // Lưu chỉ số mới
        UtilityReading savedReading = utilityReadingRepository.save(newReading);

        // Tạo hóa đơn từ chỉ số mới
        Invoice invoice = invoiceService.createInvoiceFromUtilityReading(savedReading, previousElectric, previousWater);

        // Đánh dấu là đã lên hóa đơn
        savedReading.setBilled(true);
        utilityReadingRepository.save(savedReading);

        return savedReading;
    }

    // Thêm lấy chi tiết tính tiền điện nước
    public Map<String, Double> getElectricCalculationDetails(double usage) {
        return UtilityCalculator.calculateElectricDetails(usage);
    }

    public Map<String, Double> getWaterCalculationDetails(double usage) {
        return UtilityCalculator.calculateWaterDetails(usage);
    }
}