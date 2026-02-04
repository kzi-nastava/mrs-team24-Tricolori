package com.example.mobile.network;

import com.example.mobile.dto.PageResponse;
import com.example.mobile.dto.ride.DriverRideDetailResponse;
import com.example.mobile.dto.ride.DriverRideHistoryResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface RideService {

    @GET("api/v1/rides/history/driver")  // ← Changed from "api/v1/driver/rides/history"
    Call<PageResponse<DriverRideHistoryResponse>> getDriverRideHistory(
            @Query("page") int page,
            @Query("size") int size
    );

    @GET("api/v1/rides/{rideId}/details/driver")  // ← Changed from "api/v1/driver/rides/{rideId}"
    Call<DriverRideDetailResponse> getDriverRideDetail(
            @Path("rideId") Long rideId
    );
}
