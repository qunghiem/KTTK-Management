package com.hostel.management.service;

import com.hostel.management.model.Room;
import com.hostel.management.repository.RoomRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Comparator;

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

    // lấy danh sách phòng nổi bật
    public List<Room> getFeaturedRooms() {
        return roomRepository.findTop4ByStatusOrderByIdDesc("available");
    }

    public List<Room> searchRoomsByFilter(String district, Float minPrice, Float maxPrice,
                                          String roomType, Float area, Integer floor, String status) {
        System.out.println("Service search: district=" + district +
                ", roomType=" + roomType +
                ", minPrice=" + minPrice +
                ", maxPrice=" + maxPrice);

        return roomRepository.findByFilter(district, minPrice, maxPrice, roomType, area, floor, status);
    }

    // Thêm phương thức mới có hỗ trợ sắp xếp
    public List<Room> searchRoomsByFilterWithSort(String district, Float minPrice, Float maxPrice,
                                                  String roomType, Float area, Integer floor, String status, String sort) {
        System.out.println("Service search with sort: district=" + district +
                ", roomType=" + roomType +
                ", minPrice=" + minPrice +
                ", maxPrice=" + maxPrice +
                ", sort=" + sort);

        List<Room> rooms = roomRepository.findByFilter(district, minPrice, maxPrice, roomType, area, floor, status);

        // Áp dụng sắp xếp
        if (sort != null) {
            switch (sort) {
                case "price_asc":
                    rooms.sort(Comparator.comparing(Room::getPrice));
                    break;
                case "price_desc":
                    rooms.sort(Comparator.comparing(Room::getPrice).reversed());
                    break;
                case "area_asc":
                    rooms.sort(Comparator.comparing(Room::getArea));
                    break;
                case "area_desc":
                    rooms.sort(Comparator.comparing(Room::getArea).reversed());
                    break;
                case "latest":
                default:
                    // Sắp xếp theo ID giảm dần (mới nhất)
                    rooms.sort(Comparator.comparing(Room::getId).reversed());
                    break;
            }
        }

        return rooms;
    }
}