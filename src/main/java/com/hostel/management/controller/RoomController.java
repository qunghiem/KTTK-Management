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
            @RequestParam(required = false) Float minPrice, // Thêm tham số này
            @RequestParam(required = false) Float maxPrice, // Thêm tham số này
            @RequestParam(required = false) Float area,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Integer floor,
            Model model) {

        List<Room> rooms;

        // Đảm bảo chỉ lấy phòng có trạng thái "available"
        if (status == null) {
            status = "available";
        }

        // Xử lý tham số priceRange nếu có
        if (priceRange != null && !priceRange.isEmpty() && (minPrice == null || maxPrice == null)) {
            String[] prices = priceRange.split("-");
            if (prices.length == 2) {
                try {
                    minPrice = Float.parseFloat(prices[0]);
                    maxPrice = Float.parseFloat(prices[1]);
                } catch (NumberFormatException e) {
                    // Xử lý lỗi - in ra log để debug
                    System.err.println("Lỗi chuyển đổi priceRange: " + priceRange);
                }
            }
        }

        // In thông tin debug
        System.out.println("Tham số tìm kiếm: district=" + district +
                ", roomType=" + roomType +
                ", minPrice=" + minPrice +
                ", maxPrice=" + maxPrice);

        // Sử dụng một phương thức duy nhất cho tìm kiếm
        rooms = roomService.searchRoomsByFilter(district, minPrice, maxPrice, roomType, area, floor, status);

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