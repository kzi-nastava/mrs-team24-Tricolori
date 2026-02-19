package com.example.mobile.dto.vehicle;

public class UpdateVehicleLocationRequest {

    private double latitude;
    private double longitude;

    public UpdateVehicleLocationRequest(double latitude, double longitude) {
        this.latitude  = latitude;
        this.longitude = longitude;
    }

    public double getLatitude()  { return latitude; }
    public double getLongitude() { return longitude; }
}