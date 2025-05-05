package com.hostel.management.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.FutureOrPresent;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.util.Date;

@Entity
@Table(name = "bookings")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customerId;

    @ManyToOne
    @JoinColumn(name = "room_id", nullable = false)
    private Room roomId;

    @Column(name = "booking_date", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date bookingDate;

    @NotNull(message = "Ngày bắt đầu không được để trống")
    @FutureOrPresent(message = "Ngày bắt đầu phải là ngày hiện tại hoặc trong tương lai")
    @Column(name = "start_date", nullable = false)
    @Temporal(TemporalType.DATE)
    private Date startDate;

    @Column(name = "end_date")
    @Temporal(TemporalType.DATE)
    private Date endDate;

    @Column(nullable = false)
    private String status;

    @NotNull(message = "Tiền đặt cọc không được để trống")
    @Positive(message = "Tiền đặt cọc phải là số dương")
    @Column(nullable = false)
    private float deposit;

    @Column(name = "duration")
    private int duration;

    @Column(name = "num_tenants")
    private int numTenants;

    @Column(name = "vehicle")
    private String vehicle;

    @Column(name = "notes")
    private String notes;
}