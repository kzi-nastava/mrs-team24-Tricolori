package com.example.mobile.network;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.annotation.NonNull;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class AuthInterceptor implements Interceptor {
    private final Context context;

    private final List<String> excludeEndpoints = List.of("api/v1/auth");
    private final List<String> specialCaseEndpoints = List.of("api/v1/auth/register-driver");

    public AuthInterceptor(Context context) {
        this.context = context;
    }

    @NonNull
    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {
        Request request = chain.request();

        Request.Builder builder = request.newBuilder()
                .header("Content-Type", "application/json; charset=utf-8")
                .header("Accept", "application/json");

        boolean shouldAddToken = shouldAddToken(request);

        if (shouldAddToken) {
            SharedPreferences prefs = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
            String token = prefs.getString("jwt_token", null);

            if (token != null) {
                builder.header("Authorization", "Bearer " + token);
            }
        }

        return chain.proceed(builder.build());
    }

    private boolean shouldAddToken(Request request) {
        String url = request.url().toString();

        boolean isExcluded = false;
        for (String endpoint : excludeEndpoints) {
            if (url.contains(endpoint)) {
                isExcluded = true;
                break;
            }
        }

        boolean isSpecialCase = false;
        for (String endpoint : specialCaseEndpoints) {
            if (url.contains(endpoint)) {
                isSpecialCase = true;
                break;
            }
        }

        return !isExcluded || isSpecialCase;
    }
}