package com.example.mobile.network.service;

import com.example.mobile.dto.PageResponse;
import com.example.mobile.dto.block.ActivePersonStatus;
import com.example.mobile.dto.block.BlockRequest;

import retrofit2.Call;
import retrofit2.http.*;

public interface PersonService {
    @GET("persons/statuses")
    Call<PageResponse<ActivePersonStatus>> getUsers(
            @Query("id") Long id,
            @Query("firstName") String firstName,
            @Query("lastName") String lastName,
            @Query("email") String email,
            @Query("page") int page,
            @Query("size") int size
    );

    @PATCH("persons/block")
    Call<Void> applyBlock(@Body BlockRequest request);

    @DELETE("persons/unblock")
    Call<Void> removeBlock(@Query("email") String email);
}
