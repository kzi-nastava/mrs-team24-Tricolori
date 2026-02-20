package com.example.mobile.network.service;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

public class StompRideService {
    private static final String TAG = "StompRideService";
    private static final String WS_URL = "ws://192.168.31.196:8080/ws";

    private static final int RECONNECT_BASE_MS = 3_000;
    private static final int MAX_RECONNECT_ATTEMPTS = 10;
    private static final long MAX_RECONNECT_DELAY_MS = 60_000L;
    private static final String NULL_BYTE = "\u0000";

    /**
     * RideListener - Callback interface za ride notifikacije
     *
     * Sve što trebam znati:
     * 1. onRideAssigned() - Vozač dobije voznju (samo Long rideId)
     * 2. onRideStatusUpdate() - Putnik/vozač vidi status update
     * 3. onConnectionStateChanged() - WebSocket se poveže/prekine
     */
    public interface RideListener {
        void onRideAssigned(long rideId);
        void onRideStatusUpdate(RideStatusUpdate update);
        void onConnectionStateChanged(boolean connected);
    }

    /**
     * RideStatusUpdate - Novi mali DTO samo za WebSocket notifikacije
     *
     * (Minimalista - samo ono što WebSocket pošalje)
     * Ako trebam sve detalje, učitam RideAssignmentResponse iz REST API-ja
     */
    public static class RideStatusUpdate {
        public String status;      // ONGOING, FINISHED, CANCELLED_BY_DRIVER, itd
        public Long rideId;
        public String message;

        public RideStatusUpdate() {}

        public RideStatusUpdate(String status, Long rideId, String message) {
            this.status = status;
            this.rideId = rideId;
            this.message = message;
        }

        public String getStatus() {
            return status;
        }

        public Long getRideId() {
            return rideId;
        }

        public String getMessage() {
            return message;
        }
    }

    // ============ WebSocket Instance ============
    private static final OkHttpClient HTTP_CLIENT = new OkHttpClient.Builder()
            .readTimeout(0, TimeUnit.MILLISECONDS)
            .connectTimeout(10, TimeUnit.SECONDS)
            .pingInterval(25, TimeUnit.SECONDS)
            .build();

    private final Gson gson = new Gson();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private WebSocket webSocket;
    private String userEmail;
    private String jwtToken;
    private RideListener listener;

    private boolean connected = false;
    private boolean intentionalDisconnect = false;
    private int reconnectAttempts = 0;

    /**
     * Poveži se na WebSocket
     */
    public void connect(String email, String jwt, RideListener listener) {
        this.userEmail = email;
        this.jwtToken = jwt;
        this.listener = listener;
        this.intentionalDisconnect = false;
        this.reconnectAttempts = 0;
        openWebSocket();
    }

    /**
     * Prekini WebSocket
     */
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
            subscribeToRideTopics(ws);

        } else if (frame.startsWith("MESSAGE")) {
            handleMessageFrame(frame);

        } else if (frame.startsWith("ERROR")) {
            Log.e(TAG, "STOMP ERROR frame:\n" + frame);
            setConnected(false);
            if (!intentionalDisconnect) scheduleReconnect();

        } else {
            Log.v(TAG, "Unhandled frame: " + frame.substring(0, Math.min(frame.length(), 40)));
        }
    }

    private void subscribeToRideTopics(WebSocket ws) {
        Log.d(TAG, "STOMP connected — subscribing to ride topics");

        // Subscribe na /user/queue/ride-assigned (za vozače)
        String subscribeAssignedFrame =
                "SUBSCRIBE\n" +
                        "id:sub-ride-assigned\n" +
                        "destination:/user/queue/ride-assigned\n" +
                        "ack:auto\n" +
                        "\n" + NULL_BYTE;
        ws.send(subscribeAssignedFrame);

        // Subscribe na /user/queue/ride-updates (za putnike i vozače)
        String subscribeUpdatesFrame =
                "SUBSCRIBE\n" +
                        "id:sub-ride-updates\n" +
                        "destination:/user/queue/ride-updates\n" +
                        "ack:auto\n" +
                        "\n" + NULL_BYTE;
        ws.send(subscribeUpdatesFrame);
    }

    /**
     * Rukuj MESSAGE frame-a
     *
     * Dva tipa notifikacija:
     * 1. Ride assignment za vozače: Samo Long (rideId)
     * 2. Ride status update: RideStatusUpdate JSON
     */
    private void handleMessageFrame(String frame) {
        int bodyStart = frame.indexOf("\n\n");
        if (bodyStart == -1) {
            Log.w(TAG, "MESSAGE frame missing body separator");
            return;
        }

        String body = frame.substring(bodyStart + 2)
                .replace(NULL_BYTE, "")
                .trim();

        Log.d(TAG, "Ride message body: " + body);

        try {
            // Pokušaj prvo parsirati kao Long (rideId za vozače)
            try {
                long rideId = Long.parseLong(body);
                mainHandler.post(() -> {
                    if (listener != null) listener.onRideAssigned(rideId);
                });
                return;
            } catch (NumberFormatException ignored) {
                // Nije Long, nastavi sa RideStatusUpdate
            }

            // Parsuj kao RideStatusUpdate (za putnike)
            RideStatusUpdate update = gson.fromJson(body, RideStatusUpdate.class);
            if (update != null) {
                mainHandler.post(() -> {
                    if (listener != null) listener.onRideStatusUpdate(update);
                });
            }

        } catch (JsonSyntaxException e) {
            Log.e(TAG, "Failed to parse ride message: " + body, e);
        } catch (Exception e) {
            Log.e(TAG, "Unexpected error parsing ride message", e);
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
