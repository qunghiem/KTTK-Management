package com.hostel.management.service;

import com.hostel.management.model.Room;
import com.hostel.management.model.UtilityReading;
import com.hostel.management.model.Invoice;
import com.hostel.management.repository.RoomRepository;
import com.hostel.management.repository.UtilityReadingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.hostel.management.util.UtilityCalculator;
import java.util.Map;
import java.util.Date;
import java.util.List;
import java.util.Calendar;
import java.util.stream.Collectors;

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

    /**
     * Lấy chỉ số cuối cùng trước tháng được chỉ định
     */
    public UtilityReading getLastReadingBeforeMonth(int roomId, int month, int year) {
        // Tạo ngày đầu tháng được chỉ định
        Calendar cal = Calendar.getInstance();
        cal.set(year, month - 1, 1, 0, 0, 0); // month - 1 vì Calendar.MONTH bắt đầu từ 0
        cal.set(Calendar.MILLISECOND, 0);
        Date startOfMonth = cal.getTime();

        Room room = roomRepository.findById(roomId).orElse(null);
        if (room == null) {
            return null;
        }

        // Lấy tất cả chỉ số trước ngày đầu tháng này, sắp xếp theo ngày giảm dần
        List<UtilityReading> readings = utilityReadingRepository.findByRoomAndReadingDateBefore(room, startOfMonth);

        return readings.isEmpty() ? null : readings.get(0);
    }

    /**
     * Kiểm tra xem phòng đã có chỉ số trong tháng được chỉ định chưa
     */
    public boolean hasReadingInMonth(int roomId, int month, int year) {
        Room room = roomRepository.findById(roomId).orElse(null);
        if (room == null) {
            return false;
        }

        // Tạo ngày đầu và cuối tháng
        Calendar startCal = Calendar.getInstance();
        startCal.set(year, month - 1, 1, 0, 0, 0);
        startCal.set(Calendar.MILLISECOND, 0);
        Date startOfMonth = startCal.getTime();

        Calendar endCal = Calendar.getInstance();
        endCal.set(year, month - 1, startCal.getActualMaximum(Calendar.DAY_OF_MONTH), 23, 59, 59);
        endCal.set(Calendar.MILLISECOND, 999);
        Date endOfMonth = endCal.getTime();

        List<UtilityReading> readings = utilityReadingRepository.findByRoomAndReadingDateBetween(room, startOfMonth, endOfMonth);
        return !readings.isEmpty();
    }

    /**
     * Lấy danh sách phòng chưa nhập chỉ số trong tháng được chỉ định
     */
    public List<Room> getRoomsWithoutReadingInMonth(int month, int year) {
        List<Room> allRooms = roomRepository.findAll();

        return allRooms.stream()
                .filter(room -> !hasReadingInMonth(room.getId(), month, year))
                .collect(Collectors.toList());
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
        newReading.setElectricTotal(electricTotal);
        newReading.setWaterTotal(waterTotal);
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

    /**
     * Lưu chỉ số cho tháng cụ thể
     */
    @Transactional
    public UtilityReading saveReadingForMonth(int roomId, Date readingDate, double electricReading, double waterReading, int month, int year) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Phòng không tồn tại"));

        // Kiểm tra xem đã có chỉ số trong tháng này chưa
        if (hasReadingInMonth(roomId, month, year)) {
            throw new RuntimeException("Phòng này đã có chỉ số trong tháng " + month + "/" + year);
        }

        // Lấy chỉ số cũ từ tháng trước
        UtilityReading lastReading = getLastReadingBeforeMonth(roomId, month, year);
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
        newReading.setElectricTotal(electricTotal);
        newReading.setWaterTotal(waterTotal);
        newReading.setBilled(false);

        // Lưu chỉ số mới
        UtilityReading savedReading = utilityReadingRepository.save(newReading);

        // Tạo hóa đơn từ chỉ số mới với thông tin tháng/năm
        Invoice invoice = invoiceService.createInvoiceFromUtilityReadingForMonth(savedReading, previousElectric, previousWater, month, year);

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