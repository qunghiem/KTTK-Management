package com.hostel.management.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "invoices")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Invoice {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @ManyToOne
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @Column(name = "create_date", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date createDate;

    @Column(name = "electric_reading", nullable = false)
    private double electricReading;

    @Column(name = "previous_electric_reading")
    private double previousElectricReading;

    @Column(name = "electric_usage", nullable = false)
    private double electricUsage;

    @Column(name = "electric_price", nullable = false)
    private double electricPrice;

    @Column(name = "electric_total", nullable = false)
    private double electricTotal;

    @Column(name = "water_reading", nullable = false)
    private double waterReading;

    @Column(name = "previous_water_reading")
    private double previousWaterReading;

    @Column(name = "water_usage", nullable = false)
    private double waterUsage;

    @Column(name = "water_price", nullable = false)
    private double waterPrice;

    @Column(name = "water_total", nullable = false)
    private double waterTotal;

    @Column(name = "room_charge", nullable = false)
    private double roomCharge;

    @Column(name = "total_amount", nullable = false)
    private double totalAmount;

    @Column(name = "paid_status", nullable = false)
    private boolean paidStatus = false;

    @Column(name = "payment_date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date paymentDate;

    @Column(name = "month")
    private int month;

    @Column(name = "year")
    private int year;

    @Column(name = "utility_reading_id")
    private int utilityReadingId;
}