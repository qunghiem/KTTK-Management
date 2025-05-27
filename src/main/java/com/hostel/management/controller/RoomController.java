package com.hostel.management.controller;

import com.hostel.management.model.Room;
import com.hostel.management.model.RoomType;
import com.hostel.management.service.RoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
public class RoomController {
    @Autowired
    private RoomService roomService;

    @GetMapping("/rooms")
    public String searchRooms(
            @RequestParam(required = false) String district,
            @RequestParam(required = false) String priceRange,
            @RequestParam(required = false) String roomType,
            @RequestParam(required = false) Float minPrice,
            @RequestParam(required = false) Float maxPrice,
            @RequestParam(required = false) Float area,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Integer floor,
            @RequestParam(required = false, defaultValue = "latest") String sort,
            Model model) {

        // Debug log
        System.out.println("=== DEBUG SEARCH PARAMETERS ===");
        System.out.println("district: " + district);
        System.out.println("roomType: " + roomType);
        System.out.println("minPrice: " + minPrice);
        System.out.println("maxPrice: " + maxPrice);
        System.out.println("area: " + area);
        System.out.println("status: " + status);
        System.out.println("floor: " + floor);
        System.out.println("sort: " + sort);

        // Thiết lập status mặc định
        if (status == null || status.isEmpty()) {
            status = "available";
        }

        List<Room> rooms;

        // Kiểm tra nếu không có tham số tìm kiếm nào, lấy tất cả phòng available
        boolean hasSearchParams = (district != null && !district.isEmpty()) ||
                (roomType != null && !roomType.isEmpty()) ||
                (minPrice != null) ||
                (maxPrice != null) ||
                (area != null) ||
                (floor != null);

        if (!hasSearchParams) {
            // Không có tham số tìm kiếm, lấy tất cả phòng available
            System.out.println("No search params, getting all available rooms");
            rooms = roomService.getRoomsByStatus(status);
        } else {
            // Có tham số tìm kiếm, sử dụng search
            System.out.println("Has search params, performing search");

            // Tạo đối tượng Room để làm criteria tìm kiếm
            Room searchCriteria = new Room();

            // Thiết lập address để tìm theo district
            if (district != null && !district.isEmpty()) {
                // Chuyển đổi district number thành tên khu vực
                String districtName = convertDistrictToName(district);
                searchCriteria.setAddress(districtName);
                System.out.println("Setting address filter: " + districtName);
            }

            // Thiết lập roomType để tìm kiếm
            if (roomType != null && !roomType.isEmpty()) {
                RoomType type = new RoomType();
                type.setName(roomType);
                searchCriteria.setRoomType(type);
                System.out.println("Setting roomType filter: " + roomType);
            }

            // Thiết lập price (sử dụng maxPrice làm tiêu chí)
            if (maxPrice != null && maxPrice > 0) {
                searchCriteria.setPrice(maxPrice);
                System.out.println("Setting maxPrice filter: " + maxPrice);
            }

            // Thiết lập area
            if (area != null && area > 0) {
                searchCriteria.setArea(area);
                System.out.println("Setting area filter: " + area);
            }

            // Thiết lập floor
            if (floor != null && floor > 0) {
                searchCriteria.setFloor(floor);
                System.out.println("Setting floor filter: " + floor);
            }

            searchCriteria.setStatus(status);
            System.out.println("Setting status filter: " + status);

            // Tìm kiếm với sắp xếp
            rooms = roomService.searchRoomsByFilterWithSort(searchCriteria, sort);

            // Lọc thêm theo minPrice nếu cần (vì có thể repository chưa hỗ trợ đầy đủ)
            if (minPrice != null && minPrice > 0) {
                System.out.println("Applying minPrice filter: " + minPrice);
                rooms = rooms.stream()
                        .filter(room -> room.getPrice() >= minPrice)
                        .collect(java.util.stream.Collectors.toList());
            }
        }

        System.out.println("Found " + rooms.size() + " rooms");

        // Debug: In ra 3 phòng đầu tiên để kiểm tra
        for (int i = 0; i < Math.min(3, rooms.size()); i++) {
            Room room = rooms.get(i);
            System.out.println("Room " + (i+1) + ": " +
                    "ID=" + room.getId() +
                    ", Number=" + room.getRoomNumber() +
                    ", Price=" + room.getPrice() +
                    ", Status=" + room.getStatus() +
                    ", Address=" + room.getAddress());
        }

        model.addAttribute("rooms", rooms);
        model.addAttribute("currentSort", sort);

        return "room/roomList";
    }

    @GetMapping("/rooms/{id}")
    public String getRoomById(@PathVariable int id, Model model) {
        Room room = roomService.getRoomById(id);

        if (room == null) {
            return "redirect:/rooms?error=room_not_found";
        }

        model.addAttribute("room", room);
        return "room/roomDetail";
    }

    @GetMapping("/available-rooms")
    public String getAvailableRooms(Model model) {
        List<Room> availableRooms = roomService.getAvailableRooms();
        System.out.println("Available rooms: " + availableRooms.size());
        model.addAttribute("rooms", availableRooms);
        return "room/roomList";
    }

    // Method để chuyển đổi district number thành tên
    private String convertDistrictToName(String district) {
        if (district == null || district.isEmpty()) {
            return null;
        }

        try {
            int districtNum = Integer.parseInt(district);
            return "Quận " + districtNum;
        } catch (NumberFormatException e) {
            // Nếu không phải số, trả về district gốc
            return district;
        }
    }

    // Method để test debug - có thể tạm thời để kiểm tra dữ liệu
    @GetMapping("/rooms/debug")
    @ResponseBody
    public String debugRooms() {
        List<Room> allRooms = roomService.getAllRooms();
        StringBuilder sb = new StringBuilder();
        sb.append("Total rooms in database: ").append(allRooms.size()).append("\n\n");

        for (Room room : allRooms) {
            sb.append("Room ID: ").append(room.getId())
                    .append(", Number: ").append(room.getRoomNumber())
                    .append(", Price: ").append(room.getPrice())
                    .append(", Status: ").append(room.getStatus())
                    .append(", Address: ").append(room.getAddress())
                    .append(", RoomType: ").append(room.getRoomType() != null ? room.getRoomType().getName() : "null")
                    .append("\n");
        }

        return sb.toString();
    }

    // Method để test available rooms
    @GetMapping("/rooms/test-available")
    @ResponseBody
    public String testAvailableRooms() {
        List<Room> availableRooms = roomService.getAvailableRooms();
        return "Available rooms count: " + availableRooms.size();
    }
}