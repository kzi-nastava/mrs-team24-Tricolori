package com.example.mobile.dto.ride;

import java.util.List;

public class RideTrackingResponse {

    private Long rideId;
    private String status;
    private VehicleLocationResponse currentLocation;
    private DetailedRouteResponse route;
    private Integer estimatedTimeMinutes;
    private String estimatedArrival;
    private String scheduledFor;
    private String startTime;
    private Double price;
    private DriverDto driver;
    private List<PassengerDto> passengers;

    // ---- Nested: matches VehicleLocationResponse on backend ----
    public static class VehicleLocationResponse {
        private Double latitude;
        private Double longitude;
        private String model;
        private String plateNum;

        public Double getLatitude() { return latitude; }
        public Double getLongitude() { return longitude; }
        public String getModel() { return model; }
        public String getPlateNum() { return plateNum; }
    }

    // ---- Nested: matches DetailedRouteResponse on backend ----
    public static class DetailedRouteResponse {
        private String pickupAddress;
        private String destinationAddress;
        private Double pickupLatitude;
        private Double pickupLongitude;
        private Double destinationLatitude;
        private Double destinationLongitude;
        private Double distanceKm;

        public String getPickupAddress() { return pickupAddress; }
        public String getDestinationAddress() { return destinationAddress; }
        public Double getPickupLatitude() { return pickupLatitude; }
        public Double getPickupLongitude() { return pickupLongitude; }
        public Double getDestinationLatitude() { return destinationLatitude; }
        public Double getDestinationLongitude() { return destinationLongitude; }
        public Double getDistanceKm() { return distanceKm; }
    }

    // ---- Nested: matches DriverDto on backend ----
    public static class DriverDto {
        private Long id;
        private String firstName;
        private String lastName;
        private String phoneNumber;
        private String email;

        public Long getId() { return id; }
        public String getFirstName() { return firstName; }
        public String getLastName() { return lastName; }
        public String getPhoneNumber() { return phoneNumber; }
        public String getEmail() { return email; }
    }

    // ---- Nested: matches PassengerDto on backend ----
    public static class PassengerDto {
        private Long id;
        private String firstName;
        private String lastName;
        private String email;

        public Long getId() { return id; }
        public String getFirstName() { return firstName; }
        public String getLastName() { return lastName; }
        public String getEmail() { return email; }
    }

    // ---- Getters ----

    public Long getRideId() { return rideId; }
    public String getStatus() { return status; }
    public VehicleLocationResponse getCurrentLocation() { return currentLocation; }
    public DetailedRouteResponse getRoute() { return route; }
    public Integer getEstimatedTimeMinutes() { return estimatedTimeMinutes; }
    public String getEstimatedArrival() { return estimatedArrival; }
    public String getScheduledFor() { return scheduledFor; }
    public String getStartTime() { return startTime; }
    public Double getPrice() { return price; }
    public DriverDto getDriver() { return driver; }
    public List<PassengerDto> getPassengers() { return passengers; }
}