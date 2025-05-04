package com.hostel.management.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.util.List;

@Entity
@Table(name = "room_types")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoomType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @NotBlank(message = "Tên loại phòng không được để trống")
    @Column(nullable = false)
    private String name;

    @NotNull(message = "Giá cơ bản không được để trống")
    @Positive(message = "Giá cơ bản phải là số dương")
    @Column(name = "base_price")
    private float basePrice;

    @OneToMany(mappedBy = "roomType")
    @JsonIgnoreProperties("roomType") // Tránh lỗi vòng lặp JSON
    private List<Room> rooms;
}