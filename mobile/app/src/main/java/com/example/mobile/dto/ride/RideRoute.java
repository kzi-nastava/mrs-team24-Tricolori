package com.example.mobile.dto.ride;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class RideRoute {
    @SerializedName("destination")
    private Stop destination;
    @SerializedName("pickup")
    private Stop pickup;
    @SerializedName("stops")
    private List<Stop> stops;

    public RideRoute() {}

    public RideRoute(Stop pickup, Stop destination, List<Stop> stops) {
        this.pickup      = pickup;
        this.destination = destination;
        this.stops       = stops;
    }

    public Stop       getPickup()      { return pickup; }
    public Stop       getDestination() { return destination; }
    public List<Stop> getStops()       { return stops; }
    public void setPickup(Stop pickup)             { this.pickup      = pickup; }
    public void setDestination(Stop destination)   { this.destination = destination; }
    public void setStops(List<Stop> stops)         { this.stops       = stops; }
}
