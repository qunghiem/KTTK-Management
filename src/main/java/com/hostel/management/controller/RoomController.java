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
    public String searchRooms(@RequestParam(required = false) Float price,
                              @RequestParam(required = false) Float area,
                              @RequestParam(required = false) String status,
                              @RequestParam(required = false) Integer floor,
                              Model model) {
        List<Room> rooms;

        if (price != null || area != null || status != null || floor != null) {
            // Tìm kiếm phòng theo tiêu chí
            rooms = roomService.searchRooms(price, area, status, floor);
        } else {
            // Hiển thị tất cả phòng có sẵn
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