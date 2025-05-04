package com.hostel.management.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

@Entity
@Table(name = "rooms")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Room {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @NotBlank(message = "Số phòng không được để trống")
    @Column(name = "room_number", nullable = false, unique = true)
    private String roomNumber;

    @NotNull(message = "Tầng không được để trống")
    @Column(nullable = false)
    private int floor;

    @NotNull(message = "Diện tích không được để trống")
    @Positive(message = "Diện tích phải là số dương")
    @Column(nullable = false)
    private float area;

    @NotNull(message = "Giá phòng không được để trống")
    @Positive(message = "Giá phòng phải là số dương")
    @Column(nullable = false)
    private float price;

    @NotBlank(message = "Trạng thái không được để trống")
    @Column(nullable = false)
    private String status;

    @Column(name = "room_image")
    private String roomImage;

    @Column(name = "room_facilities")
    private String roomFacilities;

    @ManyToOne
    @JoinColumn(name = "room_type_id")
    private RoomType roomType;
}