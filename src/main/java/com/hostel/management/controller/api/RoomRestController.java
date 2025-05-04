package com.hostel.management.controller.api;

import com.hostel.management.model.Room;
import com.hostel.management.repository.RoomRepository;
import com.hostel.management.repository.RoomTypeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rooms")
public class RoomRestController {

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private RoomTypeRepository roomTypeRepository;

    @GetMapping
    public List<Room> getAllRooms() {
        return roomRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Room> getRoomById(@PathVariable int id) {
        return roomRepository.findById(id)
                .map(room -> ResponseEntity.ok().body(room))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Room> createRoom(@RequestBody Room room) {
        // Kiểm tra nếu room type tồn tại
        if (room.getRoomType() != null && room.getRoomType().getId() > 0) {
            roomTypeRepository.findById(room.getRoomType().getId())
                    .orElseThrow(() -> new RuntimeException("RoomType không tồn tại"));
        }

        Room newRoom = roomRepository.save(room);
        return ResponseEntity.status(HttpStatus.CREATED).body(newRoom);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Room> updateRoom(@PathVariable int id, @RequestBody Room room) {
        return roomRepository.findById(id)
                .map(existingRoom -> {
                    room.setId(id);

                    // Kiểm tra nếu room type tồn tại
                    if (room.getRoomType() != null && room.getRoomType().getId() > 0) {
                        roomTypeRepository.findById(room.getRoomType().getId())
                                .orElseThrow(() -> new RuntimeException("RoomType không tồn tại"));
                    }

                    Room updatedRoom = roomRepository.save(room);
                    return ResponseEntity.ok().body(updatedRoom);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRoom(@PathVariable int id) {
        return roomRepository.findById(id)
                .map(room -> {
                    roomRepository.delete(room);
                    return ResponseEntity.ok().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }
}