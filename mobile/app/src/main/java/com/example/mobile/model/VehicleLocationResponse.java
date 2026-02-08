package com.example.mobile.model;

import com.google.gson.annotations.SerializedName;

// Model to match the DTO comming from backend
// Gson automatically maps JSON fields based on @SerializedName annotations

public class VehicleLocationResponse {

    @SerializedName("vehicleId")
    private Long vehicleId;

    @SerializedName("model")
    private String model;

    @SerializedName("plateNum")
    private String plateNum;

    @SerializedName("latitude")
    private Double latitude;

    @SerializedName("longitude")
    private Double longitude;

    @SerializedName("available")
    private boolean available;

    // --- Getters ---

    public Long getVehicleId() {
        return vehicleId;
    }

    public String getModel() {
        return model;
    }

    public String getPlateNum() {
        return plateNum;
    }

    public Double getLatitude() {
        return latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public boolean isAvailable() {
        return available;
    }
}