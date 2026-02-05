package com.example.mobile.network.service;

import com.example.mobile.dto.profile.ChangeDataRequestResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface ChangeDataRequestService {
    @GET("/api/v1/change-requests")
    Call<List<ChangeDataRequestResponse>> getAllPendingRequests();

    @PUT("/api/v1/change-requests/approve/{requestId}")
    Call<Void> approveRequest(@Path("requestId") long requestId);

    @PUT("/api/v1/change-requests/reject/{requestId}")
    Call<Void> rejectRequest(@Path("requestId") long requestId);
}
