package com.hostel.management.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "utility_readings")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UtilityReading {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @Column(name = "reading_date", nullable = false)
    @Temporal(TemporalType.DATE)
    private Date readingDate;

    @Column(name = "electric_reading", nullable = false)
    private double electricReading;

    @Column(name = "water_reading", nullable = false)
    private double waterReading;

    @Column(name = "electric_total")
    private double electricTotal;

    @Column(name = "water_total")
    private double waterTotal;

    @Column(name = "is_billed", nullable = false)
    private boolean billed = false;
}