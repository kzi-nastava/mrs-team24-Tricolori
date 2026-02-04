package com.example.mobile.network.service;

import com.example.mobile.dto.profile.ProfileRequest;
import com.example.mobile.dto.profile.ProfileResponse;

import java.util.Map;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;

public interface ProfileService {
    @GET("/api/v1/profiles/me")
    Call<ProfileResponse> getUserProfile();

    @PUT("/api/v1/profiles/me")
    Call<ProfileResponse> updateProfile(@Body ProfileRequest request);

    @Multipart
    @POST("/api/v1/profiles/upload-pfp")
    Call<Map<String, String>> uploadPfp(@Part MultipartBody.Part pfpFile);
}
