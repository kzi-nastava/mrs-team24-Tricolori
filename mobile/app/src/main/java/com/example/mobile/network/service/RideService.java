package com.example.mobile.network.service;

import com.example.mobile.dto.ride.CancellationRequest;
import com.example.mobile.dto.ride.DriverRideDetailResponse;
import com.example.mobile.dto.ride.DriverRideHistoryResponse;
import com.example.mobile.dto.ride.InconsistencyReportRequest;
import com.example.mobile.dto.ride.PanicRideRequest;
import com.example.mobile.dto.ride.PassengerRideDetailResponse;
import com.example.mobile.dto.ride.PassengerRideHistoryResponse;
import com.example.mobile.dto.ride.RideRatingRequest;
import com.example.mobile.dto.ride.RideRatingStatusResponse;
import com.example.mobile.dto.ride.RideTrackingResponse;
import com.example.mobile.dto.ride.StopRideRequest;
import com.example.mobile.dto.ride.StopRideResponse;
import com.example.mobile.dto.vehicle.UpdateVehicleLocationRequest;

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
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

    @GET("api/v1/rides/passenger")
    Call<ResponseBody> getPassengerRideHistory(
            @Query("startDate") String startDate,
            @Query("endDate") String endDate,
            @Query("page") int page,
            @Query("size") int size,
            @Query("sort") String sort
    );

    @GET("api/v1/rides/{rideId}/details/passenger")
    Call<PassengerRideDetailResponse> getPassengerRideDetail(
            @Path("rideId") Long rideId
    );

    @POST("api/v1/rides/{rideId}/rate")
    Call<ResponseBody> rateRide(
            @Path("rideId") Long rideId,
            @Body RideRatingRequest request
    );

    @GET("api/v1/rides/{rideId}/rating-status")
    Call<RideRatingStatusResponse> getRatingStatus(
            @Path("rideId") Long rideId
    );
  
    @PUT("api/v1/rides/{id}/cancel")
    Call<ResponseBody> cancelRide(@Path("id") Long rideId, @Body CancellationRequest request);

    @GET("api/v1/rides/{rideId}/track")
    Call<RideTrackingResponse> trackRide(
            @Path("rideId") Long rideId
    );

    @PUT("api/v1/rides/panic")
    Call<Void> panicRide(@Body PanicRideRequest request);

    @POST("api/v1/rides/{rideId}/report-inconsistency")
    Call<Void> reportInconsistency(
            @Path("rideId") Long rideId,
            @Body InconsistencyReportRequest request
    );

    @PUT("api/v1/rides/{rideId}/complete")
    Call<Void> completeRide(
            @Path("rideId") Long rideId
    );

    @PUT("api/v1/rides/stop")
    Call<StopRideResponse> stopRide(
            @Body StopRideRequest request
    );

    @PUT("api/v1/rides/{rideId}/vehicle-location")
    Call<Void> updateRideVehicleLocation(
            @Path("rideId") Long rideId,
            @Body UpdateVehicleLocationRequest request
    );
}