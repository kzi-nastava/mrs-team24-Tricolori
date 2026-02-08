package com.example.mobile.dto.profile;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.time.LocalDateTime;

public class ChangeDataRequestResponse {
    @SerializedName("id")
    @Expose
    private Long id;

    @SerializedName("driverId")
    @Expose
    private Long driverId;

    @SerializedName("email")
    @Expose
    private String email;

    @SerializedName("oldValues")
    @Expose
    private ChangeDriverProfileDTO oldValues;

    @SerializedName("newValues")
    @Expose
    private ChangeDriverProfileDTO newValues;

    @SerializedName("createdAt")
    @Expose
    private LocalDateTime createdAt;

    /*--- Getters & Setters ---*/
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }

    public Long getDriverId() {
        return driverId;
    }
    public void setDriverId(Long driverId) {
        this.driverId = driverId;
    }

    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }

    public ChangeDriverProfileDTO getOldValues() {
        return oldValues;
    }
    public void setOldValues(ChangeDriverProfileDTO oldValues) {
        this.oldValues = oldValues;
    }

    public ChangeDriverProfileDTO getNewValues() {
        return newValues;
    }
    public void setNewValues(ChangeDriverProfileDTO newValues) {
        this.newValues = newValues;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

}