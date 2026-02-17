package com.example.mobile.dto.ride;

public class RideTrackingResponse {

    private Long id;
    private String status;
    private LocationData currentLocation;
    private LocationData passengerLocation;
    private DriverData driver;
    private VehicleData vehicle;
    private Double distance;
    private Integer duration;

    public static class LocationData {
        private Double latitude;
        private Double longitude;

        public Double getLatitude() { return latitude; }
        public void setLatitude(Double latitude) { this.latitude = latitude; }
        public Double getLongitude() { return longitude; }
        public void setLongitude(Double longitude) { this.longitude = longitude; }
    }

    public static class DriverData {
        private String firstName;
        private String lastName;

        public String getFirstName() { return firstName; }
        public void setFirstName(String firstName) { this.firstName = firstName; }
        public String getLastName() { return lastName; }
        public void setLastName(String lastName) { this.lastName = lastName; }
    }

    public static class VehicleData {
        private String model;
        private String plateNum;

        public String getModel() { return model; }
        public void setModel(String model) { this.model = model; }
        public String getPlateNum() { return plateNum; }
        public void setPlateNum(String plateNum) { this.plateNum = plateNum; }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocationData getCurrentLocation() { return currentLocation; }
    public void setCurrentLocation(LocationData currentLocation) { this.currentLocation = currentLocation; }
    public LocationData getPassengerLocation() { return passengerLocation; }
    public void setPassengerLocation(LocationData passengerLocation) { this.passengerLocation = passengerLocation; }
    public DriverData getDriver() { return driver; }
    public void setDriver(DriverData driver) { this.driver = driver; }
    public VehicleData getVehicle() { return vehicle; }
    public void setVehicle(VehicleData vehicle) { this.vehicle = vehicle; }
    public Double getDistance() { return distance; }
    public void setDistance(Double distance) { this.distance = distance; }
    public Integer getDuration() { return duration; }
    public void setDuration(Integer duration) { this.duration = duration; }
}