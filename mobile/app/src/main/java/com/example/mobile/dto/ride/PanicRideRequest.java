package com.example.mobile.dto.ride;

public class PanicRideRequest {

    private VehicleLocation vehicleLocation;

    public static class VehicleLocation {
        private Double lat;
        private Double lng;

        public VehicleLocation() {}

        public VehicleLocation(Double lat, Double lng) {
            this.lat = lat;
            this.lng = lng;
        }

        public Double getLat() { return lat; }
        public void setLat(Double lat) { this.lat = lat; }
        public Double getLng() { return lng; }
        public void setLng(Double lng) { this.lng = lng; }
    }

    public PanicRideRequest() {}

    public PanicRideRequest(VehicleLocation vehicleLocation) {
        this.vehicleLocation = vehicleLocation;
    }

    public VehicleLocation getVehicleLocation() { return vehicleLocation; }
    public void setVehicleLocation(VehicleLocation vehicleLocation) { this.vehicleLocation = vehicleLocation; }
}