package com.example.mobile.model;

import java.io.Serializable;

public class RideAssignmentResponse implements Serializable {
    public Long id;
    public Double price;
    public String status;

    public String passengerFirstName;
    public String passengerLastName;
    public String passengerEmail;
    public String passengerPhoneNum;

    public String driverFirstName;
    public String driverLastName;
    public String driverEmail;
    public String driverPhoneNum;

    public String vehiclePlateNum;
    public String vehicleModel;

    public String routeGeometry;
    public Double distanceKm;
    public Long estimatedTimeSeconds;
    public String pickupAddress;
    public String destinationAddress;

    public String getPassengerFullName() {
        return passengerFirstName + " " + passengerLastName;
    }

    public String getDriverFullName() {
        return driverFirstName + " " + driverLastName;
    }

    public String getVehicleInfo() {
        return vehicleModel + " (" + vehiclePlateNum + ")";
    }

}