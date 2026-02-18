package com.example.mobile.network.service;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.example.mobile.dto.chat.ChatMessageDto;
import com.example.mobile.dto.chat.SendMessageRequest;
import com.google.gson.Gson;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okhttp3.Response;

import java.util.concurrent.TimeUnit;

public class StompChatService {

    private static final String TAG = "StompChatService";
    private static final String WS_URL = "ws://192.168.1.7:8080/ws";

    private WebSocket webSocket;
    private final Gson gson = new Gson();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private long subscribedUserId;
    private MessageListener messageListener;
    private boolean connected = false;
    private String jwtToken;

    private static final String STOMP_DISCONNECT = "DISCONNECT\n\n\u0000";

    public interface MessageListener {
        void onMessageReceived(ChatMessageDto message);
    }

    public void connect(long userId, MessageListener listener, Context context) {
        this.subscribedUserId = userId;
        this.messageListener  = listener;

        SharedPreferences prefs = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        jwtToken = prefs.getString("jwt_token", null);

        OkHttpClient client = new OkHttpClient.Builder()
                .readTimeout(0, TimeUnit.MILLISECONDS)
                .build();

        Request request = new Request.Builder()
                .url(WS_URL)
                .build();

        webSocket = client.newWebSocket(request, new WebSocketListener() {

            @Override
            public void onOpen(WebSocket ws, Response response) {
                Log.d(TAG, "WebSocket opened, sending STOMP CONNECT");

                String connectFrame;
                if (jwtToken != null) {
                    connectFrame =
                            "CONNECT\n" +
                                    "accept-version:1.1,1.2\n" +
                                    "heart-beat:4000,4000\n" +
                                    "Authorization:Bearer " + jwtToken + "\n" +
                                    "\n\u0000";
                } else {
                    connectFrame =
                            "CONNECT\n" +
                                    "accept-version:1.1,1.2\n" +
                                    "heart-beat:4000,4000\n" +
                                    "\n\u0000";
                }

                ws.send(connectFrame);
            }

            @Override
            public void onMessage(WebSocket ws, String text) {
                Log.d(TAG, "Frame received: " + text.substring(0, Math.min(text.length(), 200)));
                handleFrame(ws, text);
            }

            @Override
            public void onFailure(WebSocket ws, Throwable t, Response response) {
                Log.e(TAG, "WebSocket failure: " + t.getMessage(), t);
                connected = false;
            }

            @Override
            public void onClosed(WebSocket ws, int code, String reason) {
                Log.w(TAG, "WebSocket closed: " + reason);
                connected = false;
            }
        });
    }

    private void handleFrame(WebSocket ws, String frame) {
        if (frame.startsWith("CONNECTED")) {
            Log.d(TAG, "STOMP handshake complete, subscribing");
            connected = true;
            String subscribeFrame =
                    "SUBSCRIBE\n" +
                            "id:sub-0\n" +
                            "destination:/topic/chat/" + subscribedUserId + "\n" +
                            "\n\u0000";
            ws.send(subscribeFrame);

        } else if (frame.startsWith("MESSAGE")) {
            int bodyStart = frame.indexOf("\n\n");
            if (bodyStart != -1) {
                String body = frame.substring(bodyStart + 2).replace("\u0000", "").trim();
                try {
                    ChatMessageDto msg = gson.fromJson(body, ChatMessageDto.class);
                    mainHandler.post(() -> {
                        if (messageListener != null) messageListener.onMessageReceived(msg);
                    });
                } catch (Exception e) {
                    Log.e(TAG, "Failed to parse MESSAGE body: " + body, e);
                }
            }

        } else if (frame.startsWith("ERROR")) {
            Log.e(TAG, "STOMP ERROR frame: " + frame);

        } else if (frame.trim().isEmpty() || frame.equals("\n")) {
            // heartbeat, ignore
            Log.v(TAG, "Heartbeat received");
        }
    }

    public void sendMessage(long senderId, long receiverId, String content) {
        if (webSocket == null || !connected) {
            Log.w(TAG, "Not connected (connected=" + connected + ", webSocket=" + webSocket + ")");
            return;
        }

        SendMessageRequest request = new SendMessageRequest(senderId, receiverId, content);
        String json = gson.toJson(request);

        String sendFrame =
                "SEND\n" +
                        "destination:/app/chat.send\n" +
                        "content-type:application/json\n" +
                        "content-length:" + json.getBytes().length + "\n" +
                        "\n" + json + "\u0000";

        Log.d(TAG, "Sending frame: " + sendFrame);
        boolean sent = webSocket.send(sendFrame);
        Log.d(TAG, "webSocket.send() returned: " + sent);
    }

    public void disconnect() {
        if (webSocket != null) {
            if (connected) webSocket.send(STOMP_DISCONNECT);
            webSocket.close(1000, "User navigated away");
            webSocket = null;
        }
        connected = false;
    }
}