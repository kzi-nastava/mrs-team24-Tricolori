package com.example.mobile.dto.ride;

import com.google.gson.annotations.SerializedName;

public class FavoriteRoute {
    @SerializedName("routeId")
    private int routeId;

    @SerializedName("title")
    private String title;

    @SerializedName("route")
    private RouteDto route;

    public FavoriteRoute() {}

    public int       getRouteId() { return routeId; }
    public String    getTitle()   { return title; }
    public RouteDto getRoute()   { return route; }
}
