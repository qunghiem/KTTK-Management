package com.hostel.management.repository;

import com.hostel.management.model.Room;
import com.hostel.management.model.UtilityReading;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface UtilityReadingRepository extends JpaRepository<UtilityReading, Integer> {

    List<UtilityReading> findByRoomOrderByReadingDateDesc(Room room);

    // Đảm bảo sắp xếp theo thứ tự giảm dần của ngày đọc
    @Query("SELECT u FROM UtilityReading u WHERE u.room.id = :roomId ORDER BY u.readingDate DESC")
    List<UtilityReading> findLatestByRoomId(@Param("roomId") int roomId);

    // Thêm phương thức mới
    List<UtilityReading> findByRoomAndReadingDateBetween(Room room, Date startDate, Date endDate);
}