package com.example.mobile.network;

import android.content.Context;

import com.example.mobile.network.service.ChangeDataRequestService;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import com.example.mobile.network.service.ProfileService;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {

    private static final String BASE_URL = "http://192.168.242.8:8080";
    private static Retrofit retrofit = null;

    public static Retrofit getClient(Context context) {
        if (retrofit == null) {
            Gson gson = new GsonBuilder()
                    .registerTypeAdapter(LocalDateTime.class, (JsonDeserializer<LocalDateTime>) (json, typeOfT, context1) ->
                            LocalDateTime.parse(json.getAsString(), DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                    .create();

            AuthInterceptor authInterceptor = new AuthInterceptor(context.getApplicationContext());

            OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .addInterceptor(authInterceptor)
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(okHttpClient)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .build();
        }
        return retrofit;
    }

    public static ChangeDataRequestService getChangeDataRequestService(Context context) {
        return getClient(context).create(ChangeDataRequestService.class);
    }
  
    public static ProfileService getProfileService(Context context) {
        return getClient(context).create(ProfileService.class);
    }
}