package com.example.mobile.dto.ride;

public class DriverRideHistoryResponse {

    private Long id;
    private String pickupAddress;
    private String destinationAddress;
    private String startDate;
    private String endDate;
    private Double price;
    private Double distance;
    private String status;

    public DriverRideHistoryResponse() {}

    public Long getId() {
        return id;
    }

    public String getPickupAddress() {
        return pickupAddress;
    }

    public String getDestinationAddress() {
        return destinationAddress;
    }

    public String getStartDate() {
        return startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public Double getPrice() {
        return price;
    }

    public Double getDistance() {
        return distance;
    }

    public String getStatus() {
        return status;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setPickupAddress(String pickupAddress) {
        this.pickupAddress = pickupAddress;
    }

    public void setDestinationAddress(String destinationAddress) {
        this.destinationAddress = destinationAddress;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public void setDistance(Double distance) {
        this.distance = distance;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
