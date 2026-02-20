package com.example.mobile.dto.ride;

public class PanicRideRequest {

    private Location vehicleLocation;

    public PanicRideRequest() {}

    public PanicRideRequest(Location vehicleLocation) {
        this.vehicleLocation = vehicleLocation;
    }

    public Location getVehicleLocation() { return vehicleLocation; }
    public void setVehicleLocation(Location vehicleLocation) { this.vehicleLocation = vehicleLocation; }
}