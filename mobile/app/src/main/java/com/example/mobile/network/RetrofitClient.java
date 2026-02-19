package com.example.mobile.network;

import android.content.Context;

import com.example.mobile.network.service.AuthService;
import com.example.mobile.network.service.ChangeDataRequestService;
import com.example.mobile.network.service.FavoriteRoutesService;
import com.example.mobile.network.service.RideService;
import com.example.mobile.network.service.PersonService;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import com.example.mobile.network.service.ProfileService;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializer;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class RetrofitClient {

    private static final String BASE_URL = "http://192.168.31.196:8080";
    private static Retrofit retrofit = null;

    public static Retrofit getClient(Context context) {
        if (retrofit == null) {
            Gson gson = new GsonBuilder()
                    .registerTypeAdapter(LocalDateTime.class, (JsonDeserializer<LocalDateTime>) (json, typeOfT, context1) ->
                            LocalDateTime.parse(json.getAsString(), DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                    .registerTypeAdapter(LocalDateTime.class, (JsonSerializer<LocalDateTime>) (src, typeOfSrc, context1) ->
                            new JsonPrimitive(src.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)))
                    .create();

            AuthInterceptor authInterceptor = new AuthInterceptor(context.getApplicationContext());

            OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .addInterceptor(authInterceptor)
                    .addInterceptor(new HttpLoggingInterceptor())
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(okHttpClient)
                    .addConverterFactory(ScalarsConverterFactory.create())
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .build();
        }
        return retrofit;
    }

    public static FavoriteRoutesService getFavoriteRoutesService(Context context) {
        return getClient(context).create(FavoriteRoutesService.class);
    }

    public static RideService getRideService(Context context) {
        return getClient(context).create(RideService.class);
    }

    public static ChangeDataRequestService getChangeDataRequestService(Context context) {
        return getClient(context).create(ChangeDataRequestService.class);
    }
  
    public static ProfileService getProfileService(Context context) {
        return getClient(context).create(ProfileService.class);
    }

    public static PersonService getPersonService(Context context) {
        return getClient(context).create(PersonService.class);
    }

    public static AuthService getAuthService(Context context) {
        return getClient(context).create(AuthService.class);
    }
}