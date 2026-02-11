package com.example.mobile.dto.auth;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class DriverPasswordSetupRequest {
    @SerializedName("token")
    @Expose
    private String token;

    @SerializedName("password")
    @Expose
    private String password;

    public DriverPasswordSetupRequest(String token, String password) {
        this.token = token;
        this.password = password;
    }

    /*--- Getters & Setters ---*/
    public void setToken(String token) {
        this.token = token;
    }
    public String getToken() {
        return token;
    }

    public void setPassword(String password) {
        this.password = password;
    }
    public String getPassword() {
        return password;
    }
}
