package com.example.mobile.network.service;

import com.example.mobile.dto.profile.ChangeDriverStatusRequest;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.PATCH;

public interface DriverDailyLogService {

    @PATCH("api/v1/driver-daily-logs/status")
    Call<Void> changeStatus(@Body ChangeDriverStatusRequest request);
}
