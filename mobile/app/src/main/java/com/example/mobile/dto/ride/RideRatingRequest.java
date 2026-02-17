package com.example.mobile.dto.ride;

public class RideRatingRequest {

    private Integer driverRating;
    private Integer vehicleRating;
    private String comment;

    public RideRatingRequest() {}

    public RideRatingRequest(Integer driverRating, Integer vehicleRating, String comment) {
        this.driverRating = driverRating;
        this.vehicleRating = vehicleRating;
        this.comment = comment;
    }

    public Integer getDriverRating() { return driverRating; }
    public void setDriverRating(Integer driverRating) { this.driverRating = driverRating; }

    public Integer getVehicleRating() { return vehicleRating; }
    public void setVehicleRating(Integer vehicleRating) { this.vehicleRating = vehicleRating; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
}