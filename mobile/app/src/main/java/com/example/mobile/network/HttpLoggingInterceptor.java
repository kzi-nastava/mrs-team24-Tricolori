package com.example.mobile.network;

import android.util.Log;

import androidx.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import okhttp3.Headers;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.Buffer;

/**
 * Comprehensive HTTP logging interceptor for debugging API communication
 * Logs all requests and responses with formatted JSON bodies
 */
public class HttpLoggingInterceptor implements Interceptor {

    private static final String TAG = "🌐 HTTP";
    private static final Charset UTF8 = StandardCharsets.UTF_8;

    @NonNull
    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {

        Request request = chain.request();

        long startTime = System.currentTimeMillis();

        logRequest(request);

        Response response;
        try {
            response = chain.proceed(request);
        } catch (Exception e) {
            Log.e(TAG, "❌ REQUEST FAILED: " + request.url());
            Log.e(TAG, "❌ Error: " + e.getMessage(), e);
            throw e;
        }

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        logResponse(response, duration);

        return response;
    }

    private void logRequest(Request request) {
        Log.d(TAG, "╔════════════════════════════════════════════════════════════════════");
        Log.d(TAG, "║ ⬆️ REQUEST");
        Log.d(TAG, "╠════════════════════════════════════════════════════════════════════");
        Log.d(TAG, "║ Method: " + request.method());
        Log.d(TAG, "║ URL: " + request.url());

        // Log headers
        Headers headers = request.headers();
        if (headers.size() > 0) {
            Log.d(TAG, "╠────────────────────────────────────────────────────────────────────");
            Log.d(TAG, "║ Headers:");
            for (int i = 0; i < headers.size(); i++) {
                String name = headers.name(i);
                String value = headers.value(i);
                // Mask sensitive headers
                if (name.equalsIgnoreCase("Authorization") && value.length() > 20) {
                    value = value.substring(0, 20) + "...***";
                }
                Log.d(TAG, "║   " + name + ": " + value);
            }
        }

        // Log request body
        RequestBody requestBody = request.body();
        if (requestBody != null) {
            try {
                Buffer buffer = new Buffer();
                requestBody.writeTo(buffer);

                MediaType contentType = requestBody.contentType();
                Charset charset = contentType != null ? contentType.charset(UTF8) : UTF8;

                String bodyString = buffer.readString(charset);

                if (bodyString.length() > 0) {
                    Log.d(TAG, "╠────────────────────────────────────────────────────────────────────");
                    Log.d(TAG, "║ Request Body:");
                    logJsonBody(bodyString, "║   ");
                }
            } catch (Exception e) {
                Log.w(TAG, "║ Could not log request body: " + e.getMessage());
            }
        }

        Log.d(TAG, "╚════════════════════════════════════════════════════════════════════");
    }

    private void logResponse(Response response, long duration) {
        Log.d(TAG, "╔════════════════════════════════════════════════════════════════════");
        Log.d(TAG, "║ ⬇️ RESPONSE");
        Log.d(TAG, "╠════════════════════════════════════════════════════════════════════");
        Log.d(TAG, "║ URL: " + response.request().url());
        Log.d(TAG, "║ Status: " + response.code() + " " + response.message());
        Log.d(TAG, "║ Duration: " + duration + "ms");

        // Log response headers
        Headers headers = response.headers();
        if (headers.size() > 0) {
            Log.d(TAG, "╠────────────────────────────────────────────────────────────────────");
            Log.d(TAG, "║ Response Headers:");
            for (int i = 0; i < headers.size(); i++) {
                Log.d(TAG, "║   " + headers.name(i) + ": " + headers.value(i));
            }
        }

        // Log response body
        ResponseBody responseBody = response.body();
        if (responseBody != null) {
            try {
                long contentLength = responseBody.contentLength();

                // Clone the body to avoid consuming it
                okio.BufferedSource source = responseBody.source();
                source.request(Long.MAX_VALUE);
                Buffer buffer = source.getBuffer().clone();

                MediaType contentType = responseBody.contentType();
                Charset charset = contentType != null ? contentType.charset(UTF8) : UTF8;

                if (contentLength != 0) {
                    String bodyString = buffer.readString(charset);

                    Log.d(TAG, "╠────────────────────────────────────────────────────────────────────");

                    if (response.isSuccessful()) {
                        Log.d(TAG, "║ ✅ Response Body (Success):");
                    } else {
                        Log.e(TAG, "║ ❌ Response Body (Error):");
                    }

                    logJsonBody(bodyString, "║   ");
                }
            } catch (Exception e) {
                Log.w(TAG, "║ Could not log response body: " + e.getMessage());
            }
        }

        if (response.isSuccessful()) {
            Log.d(TAG, "╚════════════════════════════════════════════════════════════════════");
        } else {
            Log.e(TAG, "╚════════════════════════════════════════════════════════════════════");
        }
    }

    /**
     * Attempts to pretty-print JSON, falls back to raw string if not valid JSON
     */
    private void logJsonBody(String body, String prefix) {
        try {
            // Try to parse as JSON object
            JSONObject jsonObject = new JSONObject(body);
            String prettyJson = jsonObject.toString(2); // Indent with 2 spaces
            logMultiLine(prettyJson, prefix);
        } catch (JSONException e1) {
            try {
                // Try to parse as JSON array
                JSONArray jsonArray = new JSONArray(body);
                String prettyJson = jsonArray.toString(2);
                logMultiLine(prettyJson, prefix);
            } catch (JSONException e2) {
                // Not JSON, just log as-is
                logMultiLine(body, prefix);
            }
        }
    }

    /**
     * Logs multi-line strings with proper prefix
     */
    private void logMultiLine(String text, String prefix) {
        String[] lines = text.split("\n");
        for (String line : lines) {
            Log.d(TAG, prefix + line);
        }
    }
}