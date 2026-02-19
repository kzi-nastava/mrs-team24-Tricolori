package com.example.mobile.network.service;

import com.example.mobile.dto.notification.NotificationDto;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface NotificationApiService {

    @GET("api/v1/notifications")
    Call<List<NotificationDto>> getAllNotifications();

    @GET("api/v1/notifications/unread")
    Call<List<NotificationDto>> getUnreadNotifications();

    @GET("api/v1/notifications/unread/count")
    Call<Integer> getUnreadCount();

    @PUT("api/v1/notifications/{id}/read")
    Call<NotificationDto> markAsRead(@Path("id") long id);

    @PUT("api/v1/notifications/read-all")
    Call<Void> markAllAsRead();

    @DELETE("api/v1/notifications/{id}")
    Call<Void> deleteNotification(@Path("id") long id);

    @DELETE("api/v1/notifications/all")
    Call<Void> deleteAllNotifications();
}