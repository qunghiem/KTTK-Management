package com.hostel.management.repository;

import com.hostel.management.model.RoomType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoomTypeRepository extends JpaRepository<RoomType, Integer> {
    // Có thể thêm các phương thức tùy chỉnh nếu cần
}