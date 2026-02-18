package com.example.mobile.dto.ride;

public class StopRideRequest {

    private Location location;

    public StopRideRequest(double latitude, double longitude) {
        this.location = new Location(latitude, longitude);
    }

    public Location getLocation() { return location; }

    public static class Location {
        private double latitude;
        private double longitude;

        public Location(double latitude, double longitude) {
            this.latitude  = latitude;
            this.longitude = longitude;
        }

        public double getLatitude()  { return latitude; }
        public double getLongitude() { return longitude; }
    }
}