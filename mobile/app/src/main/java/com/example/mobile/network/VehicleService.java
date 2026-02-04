package com.example.mobile.network;

import java.util.List;

import com.example.mobile.model.VehicleLocationResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface VehicleService {

    @GET("/api/v1/vehicles/active")
    Call<List<VehicleLocationResponse>> getAllActive();

    @GET("/api/v1/vehicles/{id}/location")
    Call<VehicleLocationResponse> getLocationById(@Path("id") Long id);
}
