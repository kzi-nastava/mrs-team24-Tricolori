package com.example.mobile.network.service;

import com.example.mobile.dto.chat.AdminAvailableResponse;
import com.example.mobile.dto.chat.AdminIdResponse;
import com.example.mobile.dto.chat.ChatMessageDto;
import com.example.mobile.dto.chat.ChatUserDto;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface ChatApiService {

    @GET("api/v1/chats/history")
    Call<List<ChatMessageDto>> getChatHistory(
            @Query("userId1") long userId1,
            @Query("userId2") long userId2
    );

    @GET("api/v1/chats/admin-available")
    Call<AdminAvailableResponse> checkAdminAvailable();

    @GET("api/v1/chats/admin-id")
    Call<AdminIdResponse> getAdminId();

    @GET("api/v1/chats/active-chats")
    Call<List<ChatUserDto>> getActiveChats(@Query("adminId") long adminId);
}