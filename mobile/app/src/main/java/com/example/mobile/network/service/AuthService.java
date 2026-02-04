package com.example.mobile.network.service;

import com.example.mobile.dto.auth.ForgotPasswordRequest;
import com.example.mobile.dto.auth.LoginRequest;
import com.example.mobile.dto.auth.LoginResponse;
import com.example.mobile.dto.auth.ResetPasswordRequest;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface AuthService {

    @POST("api/v1/auth/login")
    Call<LoginResponse> login(@Body LoginRequest request);

    @POST("api/v1/auth/forgot-password")
    Call<ResponseBody> forgotPassword(@Body ForgotPasswordRequest request);

    @POST("api/v1/auth/reset-password")
    Call<ResponseBody> resetPassword(@Body ResetPasswordRequest request);
}
