package com.example.mobile.network.service;

import com.example.mobile.dto.ride.FavoriteRoute;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface FavoriteRoutesService {
    @GET("/api/v1/favorite-routes")
    Call<List<FavoriteRoute>> getFavoriteRoutes();
}
