package com.hostel.management.repository;

import com.hostel.management.model.Booking;
import com.hostel.management.model.Customer;
import com.hostel.management.model.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Integer> {
    List<Booking> findByCustomerId(Customer customerId);

    List<Booking> findByRoomId(Room roomId);

    List<Booking> findByStatus(String status);

    List<Booking> findByRoomIdAndStatus(Room roomId, String status);

    List<Booking> findByRoomIdAndStartDateBetween(Room room, Date startDate, Date endDate);
}