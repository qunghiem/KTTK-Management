package com.hostel.management.service;

import com.hostel.management.model.Booking;
import com.hostel.management.model.Customer;
import com.hostel.management.model.Room;
import com.hostel.management.repository.BookingRepository;
import com.hostel.management.repository.RoomRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class BookingService {
    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Transactional
    public Booking createBooking(Booking booking) {
        // Kiểm tra phòng có sẵn
        Room room = booking.getRoomId();
        if (!room.getStatus().equals("available")) {
            throw new RuntimeException("Phòng không có sẵn để đặt");
        }

        // Thiết lập ngày đặt phòng và trạng thái
        booking.setBookingDate(new Date());
        booking.setStatus("pending");

        // Tính toán tiền đặt cọc (30% giá phòng)
        float deposit = calculateDeposit(room.getPrice());
        booking.setDeposit(deposit);

        return bookingRepository.save(booking);
    }

    @Transactional
    public Booking confirmBooking(int bookingId) {
        Optional<Booking> bookingOpt = bookingRepository.findById(bookingId);

        if (!bookingOpt.isPresent()) {
            throw new RuntimeException("Không tìm thấy đơn đặt phòng");
        }

        Booking booking = bookingOpt.get();
        booking.setStatus("confirmed");

        // Cập nhật trạng thái phòng
        Room room = booking.getRoomId();
        room.setStatus("booked");
        roomRepository.save(room);

        return bookingRepository.save(booking);
    }

    @Transactional
    public Booking cancelBooking(int bookingId) {
        Optional<Booking> bookingOpt = bookingRepository.findById(bookingId);

        if (!bookingOpt.isPresent()) {
            throw new RuntimeException("Không tìm thấy đơn đặt phòng");
        }

        Booking booking = bookingOpt.get();
        booking.setStatus("cancelled");

        // Cập nhật trạng thái phòng
        Room room = booking.getRoomId();
        room.setStatus("available");
        roomRepository.save(room);

        return bookingRepository.save(booking);
    }

    public Booking getBookingById(int id) {
        Optional<Booking> booking = bookingRepository.findById(id);
        return booking.orElse(null);
    }

    public float calculateDeposit(float roomPrice) {
        return roomPrice * 0.3f; // 30% giá phòng
    }
}