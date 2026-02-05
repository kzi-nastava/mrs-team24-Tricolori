package com.example.mobile.dto.vehicle;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class VehicleDto {
    @SerializedName("model")
    @Expose
    private String model;

    @SerializedName("type")
    @Expose
    private String type;

    @SerializedName("plateNumber")
    @Expose
    private String plateNumber;

    @SerializedName("numSeats")
    @Expose
    private Integer numSeats;

    @SerializedName("babyFriendly")
    @Expose
    private Boolean babyFriendly = false;

    @SerializedName("petFriendly")
    @Expose
    private Boolean petFriendly = false;

    /*--- Getters & Setters ---*/
    public String getModel() {
        return model;
    }
    public void setModel(String model) {
        this.model = model;
    }

    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
    }

    public String getPlateNumber() {
        return plateNumber;
    }
    public void setPlateNumber(String plateNumber) {
        this.plateNumber = plateNumber;
    }

    public Integer getNumSeats() {
        return numSeats;
    }
    public void setNumSeats(Integer numSeats) {
        this.numSeats = numSeats;
    }

    public Boolean getBabyFriendly() {
        return babyFriendly;
    }
    public void setBabyFriendly(Boolean babyFriendly) {
        this.babyFriendly = babyFriendly;
    }

    public Boolean getPetFriendly() {
        return petFriendly;
    }
    public void setPetFriendly(Boolean petFriendly) {
        this.petFriendly = petFriendly;
    }
}
