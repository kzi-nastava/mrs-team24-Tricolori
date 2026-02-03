package com.example.mobile.clients;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Path;
import com.example.mobile.model.VehicleLocationResponse;

// Interface describing what method we're using and what we expect
public interface VehicleService {

    @Headers({
            "User-Agent: Mobile-Android",
            "Content-Type:application/json"
    })
    @GET("vehicles/active")
    Call<ArrayList<VehicleLocationResponse>> getAllActive();

    @Headers({
            "User-Agent: Mobile-Android",
            "Content-Type:application/json"
    })
    @GET("vehicles/{id}/location")
    Call<VehicleLocationResponse> getLocationById(@Path("id") Long id);
}