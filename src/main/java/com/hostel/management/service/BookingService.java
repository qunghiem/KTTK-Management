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
        float deposit = calculateDeposit(room);
        booking.setDeposit(deposit);

        return bookingRepository.save(booking);
    }

    @Transactional
    public Booking confirmBooking(Booking booking) {
        Optional<Booking> bookingOpt = bookingRepository.findById(booking.getId());

        if (!bookingOpt.isPresent()) {
            throw new RuntimeException("Không tìm thấy đơn đặt phòng");
        }

        Booking existingBooking = bookingOpt.get();
        existingBooking.setStatus("confirmed");

        // Cập nhật trạng thái phòng
        Room room = existingBooking.getRoomId();
        room.setStatus("booked"); // Đảm bảo sử dụng "booked" thống nhất
        roomRepository.save(room);

        return bookingRepository.save(existingBooking);
    }

    @Transactional
    public Booking cancelBooking(Booking booking) {
        Optional<Booking> bookingOpt = bookingRepository.findById(booking.getId());

        if (!bookingOpt.isPresent()) {
            throw new RuntimeException("Không tìm thấy đơn đặt phòng");
        }

        Booking existingBooking = bookingOpt.get();
        existingBooking.setStatus("cancelled");

        // Cập nhật trạng thái phòng
        Room room = existingBooking.getRoomId();
        room.setStatus("available");
        roomRepository.save(room);

        return bookingRepository.save(existingBooking);
    }

    public Booking getBookingById(int id) {
        Optional<Booking> booking = bookingRepository.findById(id);
        return booking.orElse(null);
    }

    public List<Booking> getBookingsByCustomer(Customer customer) {
        return bookingRepository.findByCustomerId(customer);
    }

    public List<Booking> getBookingsByRoom(Room room) {
        return bookingRepository.findByRoomId(room);
    }

    public List<Booking> getBookingsByStatus(String status) {
        return bookingRepository.findByStatus(status);
    }

    @Transactional
    public Booking updateBooking(Booking booking) {
        return bookingRepository.save(booking);
    }

    private float calculateDeposit(Room room) {
        return room.getPrice() * 0.3f; // 30% giá phòng
    }
}