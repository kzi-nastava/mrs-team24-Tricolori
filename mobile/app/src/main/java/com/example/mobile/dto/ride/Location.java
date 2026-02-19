package com.example.mobile.dto.ride;

public class Location {
    private Double longitude;
    private Double latitude;

    public Location() {}

    public Location(Double longitude, Double latitude) {
        this.longitude = longitude;
        this.latitude  = latitude;
    }

    public Double getLongitude() { return longitude; }
    public Double getLatitude()  { return latitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }
    public void setLatitude(Double latitude)   { this.latitude  = latitude; }
}
