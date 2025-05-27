package com.hostel.management.service;

import com.hostel.management.model.Room;
import com.hostel.management.model.RoomType;
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
        List<Room> rooms = roomRepository.findAll();
        System.out.println("getAllRooms() returned: " + rooms.size() + " rooms");
        return rooms;
    }

    public Room getRoomById(int id) {
        Optional<Room> room = roomRepository.findById(id);
        return room.orElse(null);
    }

    public Room getRoomByNumber(String roomNumber) {
        return roomRepository.findByRoomNumber(roomNumber);
    }

    public Room createRoom(Room room) {
        // Kiểm tra số phòng đã tồn tại
        if (roomRepository.findByRoomNumber(room.getRoomNumber()) != null) {
            throw new RuntimeException("Số phòng đã tồn tại");
        }

        // Thiết lập trạng thái mặc định
        if (room.getStatus() == null || room.getStatus().isEmpty()) {
            room.setStatus("available");
        }

        return roomRepository.save(room);
    }

    public Room updateRoom(Room room) {
        Room existingRoom = roomRepository.findById(room.getId()).orElse(null);
        if (existingRoom == null) {
            throw new RuntimeException("Không tìm thấy phòng");
        }

        // Kiểm tra số phòng nếu thay đổi
        if (!existingRoom.getRoomNumber().equals(room.getRoomNumber())) {
            Room roomWithSameNumber = roomRepository.findByRoomNumber(room.getRoomNumber());
            if (roomWithSameNumber != null && roomWithSameNumber.getId() != room.getId()) {
                throw new RuntimeException("Số phòng đã tồn tại");
            }
        }

        return roomRepository.save(room);
    }

    public void deleteRoom(Room room) {
        roomRepository.delete(room);
    }

    public List<Room> searchRooms(Room searchCriteria) {
        Float price = (searchCriteria.getPrice() > 0) ? searchCriteria.getPrice() : null;
        Float area = (searchCriteria.getArea() > 0) ? searchCriteria.getArea() : null;
        Integer floor = (searchCriteria.getFloor() > 0) ? searchCriteria.getFloor() : null;

        return roomRepository.findBySearchCriteria(
                price,
                area,
                searchCriteria.getStatus(),
                floor
        );
    }

    public List<Room> getAvailableRooms() {
        List<Room> rooms = roomRepository.findByStatus("available");
        System.out.println("getAvailableRooms() returned: " + rooms.size() + " rooms");
        return rooms;
    }

    public List<Room> getRoomsByStatus(String status) {
        List<Room> rooms = roomRepository.findByStatus(status);
        System.out.println("getRoomsByStatus(" + status + ") returned: " + rooms.size() + " rooms");
        return rooms;
    }

    // lấy danh sách phòng nổi bật
    public List<Room> getFeaturedRooms() {
        List<Room> rooms = roomRepository.findTop4ByStatusOrderByIdDesc("available");
        System.out.println("getFeaturedRooms() returned: " + rooms.size() + " rooms");
        return rooms;
    }

    public List<Room> searchRoomsByFilter(Room filterCriteria) {
        String district = null;
        if (filterCriteria.getAddress() != null && !filterCriteria.getAddress().isEmpty()) {
            district = filterCriteria.getAddress();
        }

        String roomTypeName = null;
        if (filterCriteria.getRoomType() != null && filterCriteria.getRoomType().getName() != null) {
            roomTypeName = filterCriteria.getRoomType().getName();
        }

        // Chuyển đổi primitive sang wrapper để có thể null
        Float maxPrice = (filterCriteria.getPrice() > 0) ? filterCriteria.getPrice() : null;
        Float area = (filterCriteria.getArea() > 0) ? filterCriteria.getArea() : null;
        Integer floor = (filterCriteria.getFloor() > 0) ? filterCriteria.getFloor() : null;

        System.out.println("=== SEARCH FILTER DEBUG ===");
        System.out.println("District: " + district);
        System.out.println("RoomType: " + roomTypeName);
        System.out.println("MaxPrice: " + maxPrice);
        System.out.println("Area: " + area);
        System.out.println("Floor: " + floor);
        System.out.println("Status: " + filterCriteria.getStatus());

        List<Room> rooms = roomRepository.findByFilter(
                district,
                null, // minPrice - tạm thời null
                maxPrice, // maxPrice
                roomTypeName,
                area,
                floor,
                filterCriteria.getStatus()
        );

        System.out.println("Repository returned: " + rooms.size() + " rooms");
        return rooms;
    }

    // Thêm phương thức mới có hỗ trợ sắp xếp
    public List<Room> searchRoomsByFilterWithSort(Room filterCriteria, String sort) {
        String district = null;
        if (filterCriteria.getAddress() != null && !filterCriteria.getAddress().isEmpty()) {
            district = filterCriteria.getAddress();
        }

        String roomTypeName = null;
        if (filterCriteria.getRoomType() != null && filterCriteria.getRoomType().getName() != null) {
            roomTypeName = filterCriteria.getRoomType().getName();
        }

        // Chuyển đổi primitive sang wrapper để có thể null
        Float maxPrice = (filterCriteria.getPrice() > 0) ? filterCriteria.getPrice() : null;
        Float area = (filterCriteria.getArea() > 0) ? filterCriteria.getArea() : null;
        Integer floor = (filterCriteria.getFloor() > 0) ? filterCriteria.getFloor() : null;

        System.out.println("=== SEARCH WITH SORT DEBUG ===");
        System.out.println("District: " + district);
        System.out.println("RoomType: " + roomTypeName);
        System.out.println("MaxPrice: " + maxPrice);
        System.out.println("Area: " + area);
        System.out.println("Floor: " + floor);
        System.out.println("Status: " + filterCriteria.getStatus());
        System.out.println("Sort: " + sort);

        List<Room> rooms;

        // Nếu không có filter criteria nào, lấy tất cả theo status
        boolean hasFilter = (district != null && !district.isEmpty()) ||
                (roomTypeName != null && !roomTypeName.isEmpty()) ||
                (maxPrice != null && maxPrice > 0) ||
                (area != null && area > 0) ||
                (floor != null && floor > 0);

        if (!hasFilter) {
            System.out.println("No filters applied, getting all rooms by status");
            rooms = roomRepository.findByStatus(filterCriteria.getStatus());
        } else {
            System.out.println("Applying filters");
            rooms = roomRepository.findByFilter(
                    district,
                    null, // minPrice
                    maxPrice, // maxPrice
                    roomTypeName,
                    area,
                    floor,
                    filterCriteria.getStatus()
            );
        }

        System.out.println("Before sorting: " + rooms.size() + " rooms");

        // Áp dụng sắp xếp
        if (sort != null && !sort.isEmpty()) {
            switch (sort) {
                case "price_asc":
                    rooms.sort(Comparator.comparing(Room::getPrice));
                    System.out.println("Sorted by price ascending");
                    break;
                case "price_desc":
                    rooms.sort(Comparator.comparing(Room::getPrice).reversed());
                    System.out.println("Sorted by price descending");
                    break;
                case "area_asc":
                    rooms.sort(Comparator.comparing(Room::getArea));
                    System.out.println("Sorted by area ascending");
                    break;
                case "area_desc":
                    rooms.sort(Comparator.comparing(Room::getArea).reversed());
                    System.out.println("Sorted by area descending");
                    break;
                case "latest":
                default:
                    // Sắp xếp theo ID giảm dần (mới nhất)
                    rooms.sort(Comparator.comparing(Room::getId).reversed());
                    System.out.println("Sorted by latest (ID desc)");
                    break;
            }
        }

        System.out.println("Final result: " + rooms.size() + " rooms");
        return rooms;
    }
}