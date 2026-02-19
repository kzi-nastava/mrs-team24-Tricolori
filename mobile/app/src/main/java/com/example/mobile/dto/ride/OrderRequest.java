package com.example.mobile.dto.ride;

import com.google.gson.annotations.SerializedName;

import java.time.LocalDateTime;

public class OrderRequest {
    @SerializedName("route")
    private RideRoute route;

    @SerializedName("preferences")
    private RidePreferences preferences;

    @SerializedName("createdAt")
    private LocalDateTime createdAt;

    @SerializedName("trackers")
    private String[] trackers;

    public OrderRequest() {}

    public OrderRequest(RideRoute route,
                        RidePreferences preferences,
                        LocalDateTime createdAt,
                        String[] trackers) {
        this.route       = route;
        this.preferences = preferences;
        this.createdAt   = createdAt;
        this.trackers    = trackers;
    }

    public RideRoute       getRoute()       { return route; }
    public RidePreferences getPreferences() { return preferences; }
    public LocalDateTime   getCreatedAt()   { return createdAt; }
    public String[]        getTrackers()    { return trackers; }
}
