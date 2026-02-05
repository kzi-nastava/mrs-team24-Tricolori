package com.example.mobile.network.service;

import com.example.mobile.dto.PageResponse;
import com.example.mobile.dto.ride.DriverRideDetailResponse;
import com.example.mobile.dto.ride.DriverRideHistoryResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface RideService {

        @GET("api/v1/rides/history/driver")
        Call<List<DriverRideHistoryResponse>> getDriverRideHistory(
                @Query("startDate") String startDate,
                @Query("endDate") String endDate,
                @Query("sortBy") String sortBy,
                @Query("sortDirection") String sortDirection
        );

    @GET("api/v1/rides/{rideId}/details/driver")
    Call<DriverRideDetailResponse> getDriverRideDetail(
            @Path("rideId") Long rideId
    );
}
