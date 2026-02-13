package com.example.mobile.network.service;

import com.example.mobile.dto.auth.DriverPasswordSetupRequest;
import com.example.mobile.dto.auth.ForgotPasswordRequest;
import com.example.mobile.dto.auth.LoginRequest;
import com.example.mobile.dto.auth.LoginResponse;
import com.example.mobile.dto.auth.ResetPasswordRequest;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface AuthService {

    @POST("api/v1/auth/login")
    Call<LoginResponse> login(@Body LoginRequest request);

    @POST("api/v1/auth/forgot-password")
    Call<ResponseBody> forgotPassword(@Body ForgotPasswordRequest request);

    @POST("api/v1/auth/reset-password")
    Call<ResponseBody> resetPassword(@Body ResetPasswordRequest request);

    @Multipart
    @POST("api/v1/auth/register-passenger")
    Call<ResponseBody> registerPassenger(
            @Part("data") RequestBody requestData,
            @Part MultipartBody.Part image
    );

    @GET("api/v1/auth/activate")
    Call<ResponseBody> activateAccount(@Query("token") String token);

    @Multipart
    @POST("api/v1/auth/register-driver")
    Call<ResponseBody> registerDriver(
            @Part("data") RequestBody data,
            @Part MultipartBody.Part image
    );

    @GET("api/v1/auth/verify-token/{token}")
    Call<String> verifyToken(@Path("token") String token);

    @POST("api/v1/auth/driver-activate")
    Call<String> driverPasswordSetup(@Body DriverPasswordSetupRequest request);
}
