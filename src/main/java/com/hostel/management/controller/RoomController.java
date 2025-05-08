package com.hostel.management.controller;

import com.hostel.management.model.Room;
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
            @RequestParam(required = false) Float price,
            @RequestParam(required = false) Float area,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Integer floor,
            Model model) {

        List<Room> rooms;


        // Đảm bảo chỉ lấy phòng có trạng thái "available"
        if (status == null) {
            status = "available";
        }
        // Xử lý tham số từ form trang chủ
        Float minPrice = null;
        Float maxPrice = null;

        if (priceRange != null && !priceRange.isEmpty()) {
            String[] prices = priceRange.split("-");
            if (prices.length == 2) {
                try {
                    minPrice = Float.parseFloat(prices[0]);
                    maxPrice = Float.parseFloat(prices[1]);
                } catch (NumberFormatException e) {
                    // Xử lý lỗi
                }
            }
        }

        // Ưu tiên tìm kiếm từ form trang chủ
        if (district != null || priceRange != null || roomType != null) {
            rooms = roomService.searchRoomsByHomeFilter(district, minPrice, maxPrice, roomType);
        }
        // Sử dụng tìm kiếm thông thường nếu có tham số
        else if (price != null || area != null || status != null || floor != null) {
            rooms = roomService.searchRooms(price, area, status, floor);
        }
        // Mặc định hiển thị tất cả phòng có sẵn
        else {
            rooms = roomService.getAvailableRooms();
        }

        model.addAttribute("rooms", rooms);
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
        model.addAttribute("rooms", availableRooms);
        return "room/roomList";
    }
}