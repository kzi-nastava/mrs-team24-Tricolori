package com.example.mobile.network.service;

import android.util.Log;

import com.example.mobile.dto.chat.ChatMessageDto;
import com.example.mobile.dto.chat.SendMessageRequest;
import com.google.gson.Gson;

import org.java_websocket.drafts.Draft_6455;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import ua.naiksoftware.stomp.Stomp;
import ua.naiksoftware.stomp.StompClient;

public class StompChatService {

    private static final String TAG = "StompChatService";
    private static final String WS_URL = "ws://10.0.2.2:8080/ws/websocket";

    private StompClient stompClient;
    private CompositeDisposable disposables = new CompositeDisposable();
    private final Gson gson = new Gson();

    public interface MessageListener {
        void onMessageReceived(ChatMessageDTO message);
    }

    public void connect(long userId, MessageListener listener) {
        stompClient = Stomp.over(Stomp.ConnectionProvider.OKHTTP, WS_URL);

        disposables.add(
                stompClient.lifecycle()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(event -> {
                            switch (event.getType()) {
                                case OPENED:
                                    Log.d(TAG, "STOMP connected");
                                    subscribeToTopic(userId, listener);
                                    break;
                                case ERROR:
                                    Log.e(TAG, "STOMP error", event.getException());
                                    break;
                                case CLOSED:
                                    Log.w(TAG, "STOMP closed");
                                    break;
                            }
                        })
        );

        stompClient.connect();
    }

    private void subscribeToTopic(long userId, MessageListener listener) {
        disposables.add(
                stompClient.topic("/topic/chat/" + userId)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                stompMessage -> {
                                    ChatMessageDTO msg = gson.fromJson(stompMessage.getPayload(), ChatMessageDTO.class);
                                    listener.onMessageReceived(msg);
                                },
                                error -> Log.e(TAG, "Topic subscription error", error)
                        )
        );
    }

    public void sendMessage(long senderId, long receiverId, String content) {
        if (stompClient == null || !stompClient.isConnected()) {
            Log.w(TAG, "STOMP not connected");
            return;
        }

        SendMessageRequest request = new SendMessageRequest(senderId, receiverId, content);
        String json = gson.toJson(request);

        disposables.add(
                stompClient.send("/app/chat.send", json)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                () -> Log.d(TAG, "Message sent"),
                                error -> Log.e(TAG, "Send error", error)
                        )
        );
    }

    public void disconnect() {
        disposables.clear();
        if (stompClient != null) {
            stompClient.disconnect();
        }
    }
}