package com.example.mobile.ui.models;

import java.io.Serializable;

public class Ride implements Serializable {
    private int id;
    private String route;
    private String startDate;
    private String endDate;
    private double price;
    private String status;
    private String startTime;
    private String endTime;
    private String duration;
    private String passengerName;
    private String passengerPhone;
    private double distance;
    private String notes;

    public Ride(int id, String route, String startDate, String endDate, double price,
                String status, String startTime, String endTime, String duration,
                String passengerName, String passengerPhone, double distance, String notes) {
        this.id = id;
        this.route = route;
        this.startDate = startDate;
        this.endDate = endDate;
        this.price = price;
        this.status = status;
        this.startTime = startTime;
        this.endTime = endTime;
        this.duration = duration;
        this.passengerName = passengerName;
        this.passengerPhone = passengerPhone;
        this.distance = distance;
        this.notes = notes;
    }

    // Getters
    public int getId() { return id; }
    public String getRoute() { return route; }
    public String getStartDate() { return startDate; }
    public String getEndDate() { return endDate; }
    public double getPrice() { return price; }
    public String getStatus() { return status; }
    public String getStartTime() { return startTime; }
    public String getEndTime() { return endTime; }
    public String getDuration() { return duration; }
    public String getPassengerName() { return passengerName; }
    public String getPassengerPhone() { return passengerPhone; }
    public double getDistance() { return distance; }
    public String getNotes() { return notes; }

    // Setters
    public void setId(int id) { this.id = id; }
    public void setRoute(String route) { this.route = route; }
    public void setStartDate(String startDate) { this.startDate = startDate; }
    public void setEndDate(String endDate) { this.endDate = endDate; }
    public void setPrice(double price) { this.price = price; }
    public void setStatus(String status) { this.status = status; }
    public void setStartTime(String startTime) { this.startTime = startTime; }
    public void setEndTime(String endTime) { this.endTime = endTime; }
    public void setDuration(String duration) { this.duration = duration; }
    public void setPassengerName(String passengerName) { this.passengerName = passengerName; }
    public void setPassengerPhone(String passengerPhone) { this.passengerPhone = passengerPhone; }
    public void setDistance(double distance) { this.distance = distance; }
    public void setNotes(String notes) { this.notes = notes; }
}