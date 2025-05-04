package com.hostel.management.controller.api;

import com.hostel.management.model.RoomType;
import com.hostel.management.repository.RoomTypeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/roomtypes")
public class RoomTypeRestController {

    @Autowired
    private RoomTypeRepository roomTypeRepository;

    @GetMapping
    public List<RoomType> getAllRoomTypes() {
        return roomTypeRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<RoomType> getRoomTypeById(@PathVariable int id) {
        return roomTypeRepository.findById(id)
                .map(roomType -> ResponseEntity.ok().body(roomType))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<RoomType> createRoomType(@RequestBody RoomType roomType) {
        RoomType newRoomType = roomTypeRepository.save(roomType);
        return ResponseEntity.status(HttpStatus.CREATED).body(newRoomType);
    }

    @PutMapping("/{id}")
    public ResponseEntity<RoomType> updateRoomType(@PathVariable int id, @RequestBody RoomType roomType) {
        return roomTypeRepository.findById(id)
                .map(existingRoomType -> {
                    roomType.setId(id);
                    RoomType updatedRoomType = roomTypeRepository.save(roomType);
                    return ResponseEntity.ok().body(updatedRoomType);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRoomType(@PathVariable int id) {
        return roomTypeRepository.findById(id)
                .map(roomType -> {
                    roomTypeRepository.delete(roomType);
                    return ResponseEntity.ok().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }
}