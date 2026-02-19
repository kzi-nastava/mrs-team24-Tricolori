package com.example.mobile.dto.ride;

public class StopRideRequest {

    private Location location;

    public StopRideRequest(double latitude, double longitude) {
        this.location = new Location(latitude, longitude);
    }

    public Location getLocation() { return location; }
}