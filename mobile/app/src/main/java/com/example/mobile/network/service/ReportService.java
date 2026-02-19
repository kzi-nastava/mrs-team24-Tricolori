package com.example.mobile.network.service;


import com.example.mobile.dto.report.ReportResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface ReportService {


    @GET("api/v1/reports/personal")
    Call<ReportResponse> getPersonalReport(
            @Query("from") String from,
            @Query("to")   String to
    );


    @GET("api/v1/reports/comprehensive")
    Call<ReportResponse> getAdminReport(
            @Query("from")            String from,
            @Query("to")              String to,
            @Query("scope")           String scope,
            @Query("individualEmail") String individualEmail
    );
}
