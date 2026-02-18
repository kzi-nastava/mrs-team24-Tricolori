package com.example.mobile.dto.ride;

public class RideRatingStatusResponse {

    private Boolean rated;
    private Boolean canRate;

    public RideRatingStatusResponse() {}

    public Boolean getRated() { return rated; }
    public void setRated(Boolean rated) { this.rated = rated; }

    public Boolean getCanRate() { return canRate; }
    public void setCanRate(Boolean canRate) { this.canRate = canRate; }
}