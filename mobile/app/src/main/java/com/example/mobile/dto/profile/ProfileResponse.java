package com.example.mobile.dto.profile;

import com.example.mobile.dto.vehicle.VehicleDto;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ProfileResponse {
    @SerializedName("email")
    @Expose
    private String email;

    @SerializedName("firstName")
    @Expose
    private String firstName;

    @SerializedName("lastName")
    @Expose
    private String lastName;

    @SerializedName("homeAddress")
    @Expose
    private String homeAddress;

    @SerializedName("phoneNumber")
    @Expose
    private String phoneNumber;

    @SerializedName("pfp")
    @Expose
    private String pfp;

    @SerializedName("vehicle")
    @Expose
    private VehicleDto vehicle;

    @SerializedName("activeHours")
    @Expose
    private Double activeHours;

    /*--- Getters & Setters ---*/
    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }

    public String getFirstName() {
        return firstName;
    }
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getHomeAddress() {
        return homeAddress;
    }
    public void setHomeAddress(String homeAddress) {
        this.homeAddress = homeAddress;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getPfp() {
        return pfp;
    }
    public void setPfp(String pfp) {
        this.pfp = pfp;
    }

    public VehicleDto getVehicle() {
        return vehicle;
    }
    public void setVehicle(VehicleDto vehicle) {
        this.vehicle = vehicle;
    }

    public Double getActiveHours() {
        return activeHours;
    }
    public void setActiveHours(Double activeHours) {
        this.activeHours = activeHours;
    }

}