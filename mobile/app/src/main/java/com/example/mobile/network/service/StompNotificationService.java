package com.example.mobile.network.service;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.example.mobile.dto.notification.NotificationDto;
import com.google.gson.Gson;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

import java.util.concurrent.TimeUnit;

public class StompNotificationService {

    private static final String TAG = "StompNotifService";
    private static final String WS_URL = "ws://192.168.1.7:8080/ws";

    private static final int  RECONNECT_BASE_MS      = 3_000;
    private static final int  MAX_RECONNECT_ATTEMPTS = 10;
    private static final long MAX_RECONNECT_DELAY_MS = 60_000L;
    private static final String NULL_BYTE            = "\u0000";

    public interface NotificationListener {
        void onNotificationReceived(NotificationDto notification);
        void onConnectionStateChanged(boolean connected);
    }

    private static final OkHttpClient HTTP_CLIENT = new OkHttpClient.Builder()
            .readTimeout(0, TimeUnit.MILLISECONDS)
            .connectTimeout(10, TimeUnit.SECONDS)
            .pingInterval(25, TimeUnit.SECONDS)
            .build();

    private final Gson    gson        = new Gson();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private WebSocket webSocket;

    private String               userEmail;
    private String               jwtToken;
    private NotificationListener listener;

    private boolean connected             = false;
    private boolean intentionalDisconnect = false;
    private int     reconnectAttempts     = 0;

    public void connect(String email, String jwt, NotificationListener listener) {
        this.userEmail             = email;
        this.jwtToken              = jwt;
        this.listener              = listener;
        this.intentionalDisconnect = false;
        this.reconnectAttempts     = 0;
        openWebSocket();
    }

    public void disconnect() {
        intentionalDisconnect = true;
        mainHandler.removeCallbacksAndMessages(null);
        if (webSocket != null) {
            if (connected) webSocket.send("DISCONNECT\n\n" + NULL_BYTE);
            webSocket.close(1000, "Client disconnected");
            webSocket = null;
        }
        setConnected(false);
    }

    public boolean isConnected() {
        return connected;
    }

    private void openWebSocket() {
        if (webSocket != null) {
            webSocket.cancel();
            webSocket = null;
        }

        Log.d(TAG, "Opening WebSocket to " + WS_URL);
        Request request = new Request.Builder().url(WS_URL).build();

        webSocket = HTTP_CLIENT.newWebSocket(request, new WebSocketListener() {

            @Override
            public void onOpen(WebSocket ws, Response response) {
                Log.d(TAG, "Socket open — sending STOMP CONNECT with JWT");
                String connectFrame =
                        "CONNECT\n" +
                                "accept-version:1.1,1.2\n" +
                                "heart-beat:4000,4000\n" +
                                "Authorization:Bearer " + jwtToken + "\n" +
                                "\n" + NULL_BYTE;
                ws.send(connectFrame);
            }

            @Override
            public void onMessage(WebSocket ws, String text) {
                Log.v(TAG, "Frame <- " + text.substring(0, Math.min(text.length(), 120))
                        .replace("\n", "\\n").replace(NULL_BYTE, "<null>"));
                handleFrame(ws, text);
            }

            @Override
            public void onFailure(WebSocket ws, Throwable t, Response response) {
                int code = response != null ? response.code() : -1;
                Log.e(TAG, "WebSocket failure (HTTP " + code + "): " + t.getMessage());
                setConnected(false);
                if (!intentionalDisconnect) scheduleReconnect();
            }

            @Override
            public void onClosed(WebSocket ws, int code, String reason) {
                Log.w(TAG, "WebSocket closed [" + code + "]: " + reason);
                setConnected(false);
                if (!intentionalDisconnect && code != 1000) scheduleReconnect();
            }
        });
    }

    private void handleFrame(WebSocket ws, String frame) {
        if (frame.trim().isEmpty()) {
            Log.v(TAG, "Heartbeat received");
            ws.send("\n");
            return;
        }

        if (frame.startsWith("CONNECTED")) {
            reconnectAttempts = 0;
            setConnected(true);

            String topic = "/topic/notifications/" + userEmail;
            Log.d(TAG, "STOMP connected — subscribing to " + topic);

            String subscribeFrame =
                    "SUBSCRIBE\n" +
                            "id:sub-notif-0\n" +
                            "destination:" + topic + "\n" +
                            "ack:auto\n" +
                            "\n" + NULL_BYTE;
            ws.send(subscribeFrame);

        } else if (frame.startsWith("MESSAGE")) {
            int bodyStart = frame.indexOf("\n\n");
            if (bodyStart == -1) {
                Log.w(TAG, "MESSAGE frame missing body separator");
                return;
            }
            String body = frame.substring(bodyStart + 2)
                    .replace(NULL_BYTE, "")
                    .trim();
            Log.d(TAG, "Notification body: " + body);
            try {
                NotificationDto dto = gson.fromJson(body, NotificationDto.class);
                if (dto != null) {
                    mainHandler.post(() -> {
                        if (listener != null) listener.onNotificationReceived(dto);
                    });
                }
            } catch (Exception e) {
                Log.e(TAG, "Failed to parse NotificationDto: " + body, e);
            }

        } else if (frame.startsWith("ERROR")) {
            Log.e(TAG, "STOMP ERROR frame:\n" + frame);
            setConnected(false);
            if (!intentionalDisconnect) scheduleReconnect();

        } else {
            Log.v(TAG, "Unhandled frame: " + frame.substring(0, Math.min(frame.length(), 40)));
        }
    }

    private void scheduleReconnect() {
        if (reconnectAttempts >= MAX_RECONNECT_ATTEMPTS) {
            Log.e(TAG, "Max reconnects reached — giving up");
            return;
        }
        long delay = Math.min(
                RECONNECT_BASE_MS * (1L << reconnectAttempts),
                MAX_RECONNECT_DELAY_MS);
        reconnectAttempts++;
        Log.d(TAG, "Reconnect #" + reconnectAttempts + " in " + delay + "ms");
        mainHandler.postDelayed(() -> {
            if (!intentionalDisconnect && !connected) openWebSocket();
        }, delay);
    }

    private void setConnected(boolean value) {
        connected = value;
        mainHandler.post(() -> {
            if (listener != null) listener.onConnectionStateChanged(value);
        });
    }
}