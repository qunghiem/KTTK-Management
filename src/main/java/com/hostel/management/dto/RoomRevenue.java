package com.hostel.management.dto;

import com.hostel.management.model.Room;

public class RoomRevenue {
    private Room room;
    private double amount;

    public RoomRevenue(Room room, double amount) {
        this.room = room;
        this.amount = amount;
    }

    public Room getRoom() {
        return room;
    }

    public void setRoom(Room room) {
        this.room = room;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }
}