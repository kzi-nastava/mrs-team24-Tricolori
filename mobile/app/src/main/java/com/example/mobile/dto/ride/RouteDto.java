package com.example.mobile.dto.ride;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class RouteDto {
    @SerializedName("pickupStop")
    private Stop pickupStop;

    @SerializedName("destinationStop")
    private Stop destinationStop;

    @SerializedName("stops")
    private List<Stop> stops;

    public RouteDto(Stop pickupStop, Stop destinationStop, List<Stop> stops) {
        this.pickupStop = pickupStop;
        this.destinationStop = destinationStop;
        this.stops = stops;
    }

    public Stop getPickupStop() {
        return pickupStop;
    }

    public void setPickupStop(Stop pickupStop) {
        this.pickupStop = pickupStop;
    }

    public Stop getDestinationStop() {
        return destinationStop;
    }

    public void setDestinationStop(Stop destinationStop) {
        this.destinationStop = destinationStop;
    }

    public List<Stop> getStops() {
        return stops;
    }

    public void setStops(List<Stop> stops) {
        this.stops = stops;
    }
}