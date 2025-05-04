package com.hostel.management.repository;

import com.hostel.management.model.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoomRepository extends JpaRepository<Room, Integer> {
    Room findByRoomNumber(String roomNumber);

    List<Room> findByStatus(String status);

    // Thêm phương thức mới để lấy 4 phòng có trạng thái "available" mới nhất
    List<Room> findTop4ByStatusOrderByIdDesc(String status);

    @Query("SELECT r FROM Room r WHERE " +
            "(:price IS NULL OR r.price <= :price) AND " +
            "(:area IS NULL OR r.area >= :area) AND " +
            "(:status IS NULL OR r.status = :status) AND " +
            "(:floor IS NULL OR r.floor = :floor)")
    List<Room> findBySearchCriteria(@Param("price") Float price,
                                    @Param("area") Float area,
                                    @Param("status") String status,
                                    @Param("floor") Integer floor);
}