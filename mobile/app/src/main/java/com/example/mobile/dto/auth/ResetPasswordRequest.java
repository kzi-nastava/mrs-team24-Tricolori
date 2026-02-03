package com.example.mobile.dto.auth;

public class ResetPasswordRequest {
    public String token;
    public String password;

    public ResetPasswordRequest(String token, String password) {
        this.token = token;
        this.password = password;
    }
}
