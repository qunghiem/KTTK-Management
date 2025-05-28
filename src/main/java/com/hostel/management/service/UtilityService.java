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

    /**
     * Lấy chỉ số cuối cùng của phòng
     */
    public UtilityReading getLastReadingByRoom(Room room) {
        if (room == null || room.getId() == 0) {
            throw new IllegalArgumentException("Room không được null và phải có ID hợp lệ");
        }

        List<UtilityReading> readings = utilityReadingRepository.findLatestByRoomId(room.getId());
        UtilityReading lastReading = readings.isEmpty() ? null : readings.get(0);

        System.out.println("Lấy chỉ số cũ cho phòng " + room.getId() + ":");
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
    public UtilityReading getLastReadingBeforeMonth(Room room, int month, int year) {
        if (room == null || room.getId() == 0) {
            throw new IllegalArgumentException("Room không được null và phải có ID hợp lệ");
        }

        if (month < 1 || month > 12) {
            throw new IllegalArgumentException("Tháng phải từ 1 đến 12");
        }

        if (year < 1900 || year > 3000) {
            throw new IllegalArgumentException("Năm không hợp lệ");
        }

        // Tạo ngày đầu tháng được chỉ định
        Calendar cal = Calendar.getInstance();
        cal.set(year, month - 1, 1, 0, 0, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Date startOfMonth = cal.getTime();

        System.out.println("=== DEBUG getLastReadingBeforeMonth ===");
        System.out.println("Room ID: " + room.getId());
        System.out.println("Target month/year: " + month + "/" + year);
        System.out.println("Start of month: " + startOfMonth);

        // Lấy tất cả chỉ số trước ngày đầu tháng này, sắp xếp theo ngày giảm dần
        List<UtilityReading> readings = utilityReadingRepository.findByRoomAndReadingDateBefore(room, startOfMonth);

        System.out.println("Found " + readings.size() + " readings before " + startOfMonth);

        // Debug: In ra các chỉ số tìm được
        for (int i = 0; i < Math.min(3, readings.size()); i++) {
            UtilityReading reading = readings.get(i);
            System.out.println("Reading " + (i+1) + ": Date=" + reading.getReadingDate() +
                    ", Electric=" + reading.getElectricReading() +
                    ", Water=" + reading.getWaterReading());
        }

        UtilityReading result = readings.isEmpty() ? null : readings.get(0);
        System.out.println("Selected reading: " + (result != null ? result.getReadingDate() : "null"));
        System.out.println("=== END DEBUG ===");

        return result;
    }

    /**
     * Kiểm tra xem phòng đã có chỉ số trong tháng được chỉ định chưa
     */
    public boolean hasReadingInMonth(Room room, int month, int year) {
        if (room == null || room.getId() == 0) {
            return false;
        }

        if (month < 1 || month > 12) {
            throw new IllegalArgumentException("Tháng phải từ 1 đến 12");
        }

        if (year < 1900 || year > 3000) {
            throw new IllegalArgumentException("Năm không hợp lệ");
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

        System.out.println("=== DEBUG hasReadingInMonth ===");
        System.out.println("Room ID: " + room.getId());
        System.out.println("Check month/year: " + month + "/" + year);
        System.out.println("Start of month: " + startOfMonth);
        System.out.println("End of month: " + endOfMonth);

        List<UtilityReading> readings = utilityReadingRepository.findByRoomAndReadingDateBetween(room, startOfMonth, endOfMonth);

        System.out.println("Found " + readings.size() + " readings in month " + month + "/" + year);
        for (UtilityReading reading : readings) {
            System.out.println("- Reading date: " + reading.getReadingDate() + ", Electric: " + reading.getElectricReading());
        }
        System.out.println("=== END DEBUG hasReadingInMonth ===");

        return !readings.isEmpty();
    }

    /**
     * Lấy danh sách phòng chưa nhập chỉ số trong tháng được chỉ định
     */
    public List<Room> getRoomsWithoutReadingInMonth(int month, int year) {
        if (month < 1 || month > 12) {
            throw new IllegalArgumentException("Tháng phải từ 1 đến 12");
        }

        if (year < 1900 || year > 3000) {
            throw new IllegalArgumentException("Năm không hợp lệ");
        }

        List<Room> allRooms = roomRepository.findAll();

        return allRooms.stream()
                .filter(room -> !hasReadingInMonth(room, month, year))
                .collect(Collectors.toList());
    }

    /**
     * Lưu chỉ số điện nước mới
     */
    @Transactional
    public UtilityReading saveReading(UtilityReading utilityReading) {
        validateUtilityReading(utilityReading);

        Room room = utilityReading.getRoom();

        // Lấy room từ database để đảm bảo tồn tại
        Room existingRoom = roomRepository.findById(room.getId()).orElse(null);
        if (existingRoom == null) {
            throw new RuntimeException("Phòng không tồn tại");
        }
        utilityReading.setRoom(existingRoom);

        // Lấy chỉ số cũ
        UtilityReading lastReading = getLastReadingByRoom(existingRoom);
        double previousElectric = lastReading != null ? lastReading.getElectricReading() : 0;
        double previousWater = lastReading != null ? lastReading.getWaterReading() : 0;

        // Validate chỉ số mới phải >= chỉ số cũ
        if (utilityReading.getElectricReading() < previousElectric) {
            throw new RuntimeException("Chỉ số điện mới không thể nhỏ hơn chỉ số cũ (" + previousElectric + ")");
        }

        if (utilityReading.getWaterReading() < previousWater) {
            throw new RuntimeException("Chỉ số nước mới không thể nhỏ hơn chỉ số cũ (" + previousWater + ")");
        }

        // Tính lượng tiêu thụ
        double electricUsage = utilityReading.getElectricReading() - previousElectric;
        double waterUsage = utilityReading.getWaterReading() - previousWater;

        // Tính chi tiết
        Map<String, Double> electricDetails = UtilityCalculator.calculateElectricDetails(electricUsage);
        Map<String, Double> waterDetails = UtilityCalculator.calculateWaterDetails(waterUsage);

        // Lấy tổng từ chi tiết (đảm bảo nhất quán)
        double electricTotal = electricDetails.get("Tổng cộng");
        double waterTotal = waterDetails.get("Tổng cộng");

        utilityReading.setElectricTotal(electricTotal);
        utilityReading.setWaterTotal(waterTotal);
        utilityReading.setBilled(false);

        // Thiết lập ngày đọc nếu chưa có
        if (utilityReading.getReadingDate() == null) {
            utilityReading.setReadingDate(new Date());
        }

        // Lưu chỉ số mới
        UtilityReading savedReading = utilityReadingRepository.save(utilityReading);

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
    public UtilityReading saveReadingForMonth(UtilityReading utilityReading, int month, int year) {
        System.out.println("=== START saveReadingForMonth ===");
        System.out.println("Target month/year: " + month + "/" + year);
        System.out.println("Original reading date: " + utilityReading.getReadingDate());

        validateUtilityReading(utilityReading);

        if (month < 1 || month > 12) {
            throw new IllegalArgumentException("Tháng phải từ 1 đến 12");
        }

        if (year < 1900 || year > 3000) {
            throw new IllegalArgumentException("Năm không hợp lệ");
        }

        Room room = utilityReading.getRoom();

        // Lấy room từ database để đảm bảo tồn tại
        Room existingRoom = roomRepository.findById(room.getId()).orElse(null);
        if (existingRoom == null) {
            throw new RuntimeException("Phòng không tồn tại");
        }
        utilityReading.setRoom(existingRoom);

        // QUAN TRỌNG: Thiết lập ngày đọc TRƯỚC KHI kiểm tra hasReadingInMonth
        Calendar readingCal = Calendar.getInstance();
        readingCal.set(year, month - 1, 15); // Giữa tháng để tránh biên
        readingCal.set(Calendar.HOUR_OF_DAY, 12);
        readingCal.set(Calendar.MINUTE, 0);
        readingCal.set(Calendar.SECOND, 0);
        readingCal.set(Calendar.MILLISECOND, 0);

        utilityReading.setReadingDate(readingCal.getTime());

        System.out.println("Set reading date to: " + utilityReading.getReadingDate());

        // Kiểm tra xem đã có chỉ số trong tháng này chưa
        if (hasReadingInMonth(existingRoom, month, year)) {
            throw new RuntimeException("Phòng này đã có chỉ số trong tháng " + month + "/" + year);
        }

        // Lấy chỉ số cũ từ tháng trước
        UtilityReading lastReading = getLastReadingBeforeMonth(existingRoom, month, year);
        double previousElectric = lastReading != null ? lastReading.getElectricReading() : 0;
        double previousWater = lastReading != null ? lastReading.getWaterReading() : 0;

        System.out.println("Previous electric: " + previousElectric + ", Previous water: " + previousWater);

        // Validate chỉ số mới phải >= chỉ số cũ
        if (utilityReading.getElectricReading() < previousElectric) {
            throw new RuntimeException("Chỉ số điện mới không thể nhỏ hơn chỉ số cũ (" + previousElectric + ")");
        }

        if (utilityReading.getWaterReading() < previousWater) {
            throw new RuntimeException("Chỉ số nước mới không thể nhỏ hơn chỉ số cũ (" + previousWater + ")");
        }

        // Tính lượng tiêu thụ
        double electricUsage = utilityReading.getElectricReading() - previousElectric;
        double waterUsage = utilityReading.getWaterReading() - previousWater;

        // Tính chi tiết
        Map<String, Double> electricDetails = UtilityCalculator.calculateElectricDetails(electricUsage);
        Map<String, Double> waterDetails = UtilityCalculator.calculateWaterDetails(waterUsage);

        // Lấy tổng từ chi tiết (đảm bảo nhất quán)
        double electricTotal = electricDetails.get("Tổng cộng");
        double waterTotal = waterDetails.get("Tổng cộng");

        utilityReading.setElectricTotal(electricTotal);
        utilityReading.setWaterTotal(waterTotal);
        utilityReading.setBilled(false);

        System.out.println("Final reading date before save: " + utilityReading.getReadingDate());

        // Lưu chỉ số mới
        UtilityReading savedReading = utilityReadingRepository.save(utilityReading);

        System.out.println("Saved reading date: " + savedReading.getReadingDate());

        // Tạo hóa đơn từ chỉ số mới với thông tin tháng/năm
        Invoice invoice = invoiceService.createInvoiceFromUtilityReadingForMonth(savedReading, previousElectric, previousWater, month, year);

        // Đánh dấu là đã lên hóa đơn
        savedReading.setBilled(true);
        utilityReadingRepository.save(savedReading);

        System.out.println("=== END saveReadingForMonth ===");

        return savedReading;
    }

    /**
     * Lấy chỉ số điện nước theo ID
     */
    public UtilityReading getUtilityReadingById(int id) {
        if (id <= 0) {
            throw new IllegalArgumentException("ID phải lớn hơn 0");
        }
        return utilityReadingRepository.findById(id).orElse(null);
    }

    /**
     * Lấy tất cả chỉ số điện nước của một phòng
     */
    public List<UtilityReading> getUtilityReadingsByRoom(Room room) {
        if (room == null || room.getId() == 0) {
            throw new IllegalArgumentException("Room không được null và phải có ID hợp lệ");
        }
        return utilityReadingRepository.findByRoomOrderByReadingDateDesc(room);
    }

    /**
     * Lấy chỉ số điện nước trong khoảng thời gian
     */
    public List<UtilityReading> getUtilityReadingsBetweenDates(Room room, Date startDate, Date endDate) {
        if (room == null || room.getId() == 0) {
            throw new IllegalArgumentException("Room không được null và phải có ID hợp lệ");
        }

        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("Ngày bắt đầu và ngày kết thúc không được null");
        }

        if (startDate.after(endDate)) {
            throw new IllegalArgumentException("Ngày bắt đầu không thể sau ngày kết thúc");
        }

        return utilityReadingRepository.findByRoomAndReadingDateBetween(room, startDate, endDate);
    }

    /**
     * Cập nhật chỉ số điện nước
     */
    @Transactional
    public UtilityReading updateUtilityReading(UtilityReading utilityReading) {
        validateUtilityReading(utilityReading);

        if (utilityReading.getId() <= 0) {
            throw new IllegalArgumentException("ID của UtilityReading phải lớn hơn 0");
        }

        UtilityReading existingReading = utilityReadingRepository.findById(utilityReading.getId()).orElse(null);
        if (existingReading == null) {
            throw new RuntimeException("Không tìm thấy chỉ số điện nước với ID: " + utilityReading.getId());
        }

        // Validate room không được thay đổi
        if (existingReading.getRoom().getId() != utilityReading.getRoom().getId()) {
            throw new RuntimeException("Không thể thay đổi phòng của chỉ số điện nước");
        }

        return utilityReadingRepository.save(utilityReading);
    }

    /**
     * Xóa chỉ số điện nước
     */
    @Transactional
    public void deleteUtilityReading(UtilityReading utilityReading) {
        if (utilityReading == null || utilityReading.getId() <= 0) {
            throw new IllegalArgumentException("UtilityReading không hợp lệ");
        }

        UtilityReading existingReading = utilityReadingRepository.findById(utilityReading.getId()).orElse(null);
        if (existingReading == null) {
            throw new RuntimeException("Không tìm thấy chỉ số điện nước với ID: " + utilityReading.getId());
        }

        // Kiểm tra xem đã có hóa đơn chưa
        if (existingReading.isBilled()) {
            throw new RuntimeException("Không thể xóa chỉ số đã được lập hóa đơn");
        }

        utilityReadingRepository.delete(existingReading);
    }

    /**
     * Lấy tất cả chỉ số điện nước
     */
    public List<UtilityReading> getAllUtilityReadings() {
        return utilityReadingRepository.findAll();
    }

    /**
     * Tìm kiếm chỉ số điện nước theo nhiều tiêu chí
     */
    public List<UtilityReading> searchUtilityReadings(Room room, Date fromDate, Date toDate, Boolean billed) {
        List<UtilityReading> results = utilityReadingRepository.findAll();

        // Lọc theo phòng
        if (room != null && room.getId() > 0) {
            results = results.stream()
                    .filter(reading -> reading.getRoom().getId() == room.getId())
                    .collect(Collectors.toList());
        }

        // Lọc theo ngày
        if (fromDate != null) {
            results = results.stream()
                    .filter(reading -> !reading.getReadingDate().before(fromDate))
                    .collect(Collectors.toList());
        }

        if (toDate != null) {
            results = results.stream()
                    .filter(reading -> !reading.getReadingDate().after(toDate))
                    .collect(Collectors.toList());
        }

        // Lọc theo trạng thái lập hóa đơn
        if (billed != null) {
            results = results.stream()
                    .filter(reading -> reading.isBilled() == billed)
                    .collect(Collectors.toList());
        }

        return results;
    }

    /**
     * Lấy chi tiết tính tiền điện theo bậc thang
     */
    public Map<String, Double> getElectricCalculationDetails(double usage) {
        if (usage < 0) {
            throw new IllegalArgumentException("Lượng điện tiêu thụ không thể âm");
        }
        return UtilityCalculator.calculateElectricDetails(usage);
    }

    /**
     * Lấy chi tiết tính tiền nước theo bậc thang
     */
    public Map<String, Double> getWaterCalculationDetails(double usage) {
        if (usage < 0) {
            throw new IllegalArgumentException("Lượng nước tiêu thụ không thể âm");
        }
        return UtilityCalculator.calculateWaterDetails(usage);
    }

    /**
     * Validate đối tượng UtilityReading
     */
    private void validateUtilityReading(UtilityReading utilityReading) {
        if (utilityReading == null) {
            throw new IllegalArgumentException("UtilityReading không được null");
        }

        if (utilityReading.getRoom() == null) {
            throw new IllegalArgumentException("Room không được null");
        }

        if (utilityReading.getRoom().getId() <= 0) {
            throw new IllegalArgumentException("Room phải có ID hợp lệ");
        }

        if (utilityReading.getElectricReading() < 0) {
            throw new IllegalArgumentException("Chỉ số điện không thể âm");
        }

        if (utilityReading.getWaterReading() < 0) {
            throw new IllegalArgumentException("Chỉ số nước không thể âm");
        }

        // COMMENT OUT hoặc sửa lại validation ngày để không kiểm tra tương lai
        // Vì chúng ta có thể cần nhập chỉ số cho tháng trong tương lai
    /*
    if (utilityReading.getReadingDate() != null && utilityReading.getReadingDate().after(new Date())) {
        throw new IllegalArgumentException("Ngày đọc chỉ số không thể trong tương lai");
    }
    */

        // Thay thế bằng validation hợp lý hơn:
        if (utilityReading.getReadingDate() != null) {
            Calendar now = Calendar.getInstance();
            Calendar readingCal = Calendar.getInstance();
            readingCal.setTime(utilityReading.getReadingDate());

            // Cho phép nhập chỉ số trong năm hiện tại và năm kế tiếp
            if (readingCal.get(Calendar.YEAR) > now.get(Calendar.YEAR) + 1) {
                throw new IllegalArgumentException("Ngày đọc chỉ số không thể quá xa trong tương lai");
            }
        }
    }

    /**
     * Kiểm tra xem chỉ số có hợp lệ không (so với chỉ số trước đó)
     */
    public boolean isValidReading(UtilityReading utilityReading) {
        try {
            validateUtilityReading(utilityReading);

            UtilityReading lastReading = getLastReadingByRoom(utilityReading.getRoom());
            if (lastReading != null) {
                return utilityReading.getElectricReading() >= lastReading.getElectricReading() &&
                        utilityReading.getWaterReading() >= lastReading.getWaterReading();
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    /**
     * Lấy danh sách phòng đã nhập chỉ số trong tháng được chỉ định
     */
    public List<Room> getRoomsWithReadingInMonth(int month, int year) {
        if (month < 1 || month > 12) {
            throw new IllegalArgumentException("Tháng phải từ 1 đến 12");
        }

        if (year < 1900 || year > 3000) {
            throw new IllegalArgumentException("Năm không hợp lệ");
        }

        List<Room> allRooms = roomRepository.findAll();

        return allRooms.stream()
                .filter(room -> hasReadingInMonth(room, month, year))
                .collect(Collectors.toList());
    }

    /**
     * Lấy chỉ số điện nước trong tháng cụ thể
     */
    public UtilityReading getReadingInMonth(Room room, int month, int year) {
        if (room == null || room.getId() == 0) {
            throw new IllegalArgumentException("Room không được null và phải có ID hợp lệ");
        }

        if (month < 1 || month > 12) {
            throw new IllegalArgumentException("Tháng phải từ 1 đến 12");
        }

        if (year < 1900 || year > 3000) {
            throw new IllegalArgumentException("Năm không hợp lệ");
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
        return readings.isEmpty() ? null : readings.get(0);
    }
}