package com.example.mobile.network.service;

import com.example.mobile.dto.pricelist.PriceConfigRequest;
import com.example.mobile.dto.pricelist.PriceConfigResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.PUT;

public interface PricelistService {

    @GET("/api/v1/pricelist/current")
    Call<PriceConfigResponse> getCurrentPricing();

    @PUT("/api/v1/pricelist/update")
    Call<Void> updatePricing(@Body PriceConfigRequest request);
}