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
}