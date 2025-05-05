package com.hostel.management.service;

import com.hostel.management.model.Room;
import com.hostel.management.repository.RoomRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class RoomService {
    @Autowired
    private RoomRepository roomRepository;

    public List<Room> getAllRooms() {
        return roomRepository.findAll();
    }

    public Room getRoomById(int id) {
        Optional<Room> room = roomRepository.findById(id);
        return room.orElse(null);
    }

    public List<Room> searchRooms(Float price, Float area, String status, Integer floor) {
        return roomRepository.findBySearchCriteria(price, area, status, floor);
    }

    public List<Room> getAvailableRooms() {
        return roomRepository.findByStatus("available");
    }

    // Thêm phương thức mới để lấy danh sách phòng nổi bật
    public List<Room> getFeaturedRooms() {
        // Lấy 4 phòng có trạng thái "available" mới nhất
        // Bạn có thể thay đổi logic này tùy theo yêu cầu
        return roomRepository.findTop4ByStatusOrderByIdDesc("available");
    }

    public List<Room> searchRoomsByHomeFilter(String district, Float minPrice, Float maxPrice, String roomType) {
        return roomRepository.findByHomeFilter(district, minPrice, maxPrice, roomType);
    }
}