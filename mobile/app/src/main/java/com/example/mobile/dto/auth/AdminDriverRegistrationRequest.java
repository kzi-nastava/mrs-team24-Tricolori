package com.example.mobile.dto.auth;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class AdminDriverRegistrationRequest {
    @SerializedName("firstName")
    @Expose
    private String firstName;

    @SerializedName("lastName")
    @Expose
    private String lastName;

    @SerializedName("phone")
    @Expose
    private String phone;

    @SerializedName("address")
    @Expose
    private String address;

    @SerializedName("email")
    @Expose
    private String email;

    @SerializedName("vehicleModel")
    @Expose
    private String vehicleModel;

    @SerializedName("vehicleType")
    @Expose
    private String vehicleType;

    @SerializedName("registrationPlate")
    @Expose
    private String registrationPlate;

    @SerializedName("seatNumber")
    @Expose
    private Integer seatNumber;

    @SerializedName("petFriendly")
    @Expose
    private Boolean petFriendly;

    @SerializedName("babyFriendly")
    @Expose
    private Boolean babyFriendly;

    public AdminDriverRegistrationRequest(String firstName, String lastName, String phone,
                                          String address, String email, String vehicleModel,
                                          String vehicleType, String registrationPlate,
                                          Integer seatNumber, Boolean petFriendly,
                                          Boolean babyFriendly) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.phone = phone;
        this.address = address;
        this.email = email;
        this.vehicleModel = vehicleModel;
        this.vehicleType = vehicleType;
        this.registrationPlate = registrationPlate;
        this.seatNumber = seatNumber;
        this.petFriendly = petFriendly;
        this.babyFriendly = babyFriendly;
    }

    /*--- Getters & Setters ---*/
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getVehicleModel() { return vehicleModel; }
    public void setVehicleModel(String vehicleModel) { this.vehicleModel = vehicleModel; }

    public String getVehicleType() { return vehicleType; }
    public void setVehicleType(String vehicleType) { this.vehicleType = vehicleType; }

    public String getRegistrationPlate() { return registrationPlate; }
    public void setRegistrationPlate(String registrationPlate) { this.registrationPlate = registrationPlate; }

    public Integer getSeatNumber() { return seatNumber; }
    public void setSeatNumber(Integer seatNumber) { this.seatNumber = seatNumber; }

    public Boolean getPetFriendly() { return petFriendly; }
    public void setPetFriendly(Boolean petFriendly) { this.petFriendly = petFriendly; }

    public Boolean getBabyFriendly() { return babyFriendly; }
    public void setBabyFriendly(Boolean babyFriendly) { this.babyFriendly = babyFriendly; }
}