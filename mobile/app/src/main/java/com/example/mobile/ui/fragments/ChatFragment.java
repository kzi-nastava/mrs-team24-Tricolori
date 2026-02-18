package com.example.mobile.ui.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mobile.R;
import com.example.mobile.dto.chat.AdminAvailableResponse;
import com.example.mobile.dto.chat.ChatMessageDto;
import com.example.mobile.network.RetrofitClient;
import com.example.mobile.network.service.ChatApiService;
import com.example.mobile.network.service.StompChatService;
import com.example.mobile.ui.adapters.ChatAdapter;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatFragment extends Fragment {

    private static final String TAG = "ChatFragment";
    private static final String ARG_CURRENT_USER_ID = "current_user_id";
    private static final String ARG_OTHER_USER_ID   = "other_user_id";
    private static final String ARG_OTHER_USER_NAME = "other_user_name";

    private long currentUserId;
    private long otherUserId;
    private String otherUserName;

    private ChatAdapter adapter;
    private EditText etMessage;
    private StompChatService stompChatService;
    private ChatApiService chatApiService;

    public static ChatFragment newInstance(long currentUserId, long otherUserId, String otherUserName) {
        ChatFragment f = new ChatFragment();
        Bundle args = new Bundle();
        args.putLong(ARG_CURRENT_USER_ID, currentUserId);
        args.putLong(ARG_OTHER_USER_ID, otherUserId);
        args.putString(ARG_OTHER_USER_NAME, otherUserName);
        f.setArguments(args);
        return f;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_chat, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getArguments() != null) {
            currentUserId = getArguments().getLong(ARG_CURRENT_USER_ID);
            otherUserId   = getArguments().getLong(ARG_OTHER_USER_ID);
            otherUserName = getArguments().getString(ARG_OTHER_USER_NAME, "Support");
        }

        RecyclerView recyclerView = view.findViewById(R.id.rvMessages);
        etMessage = view.findViewById(R.id.etMessage);
        ImageButton btnSend = view.findViewById(R.id.btnSend);
        ((TextView) view.findViewById(R.id.tvOtherName)).setText(otherUserName);

        adapter = new ChatAdapter(currentUserId);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);

        chatApiService = RetrofitClient.getClient(requireContext()).create(ChatApiService.class);

        loadHistory();
        connectWebSocket();

        btnSend.setOnClickListener(v -> sendMessage());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (stompChatService != null) stompChatService.disconnect();
    }

    private void loadHistory() {
        chatApiService.getChatHistory(currentUserId, otherUserId)
                .enqueue(new Callback<List<ChatMessageDto>>() {
                    @Override
                    public void onResponse(Call<List<ChatMessageDto>> call,
                                           Response<List<ChatMessageDto>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            adapter.setMessages(response.body());
                            scrollToBottom();
                        }
                    }
                    @Override
                    public void onFailure(Call<List<ChatMessageDto>> call, Throwable t) {
                        Log.e(TAG, "loadHistory failed", t);
                    }
                });
    }

    private void connectWebSocket() {
        stompChatService = new StompChatService();
        stompChatService.connect(currentUserId, message -> {
            if (!isAdded()) return;
            if ((message.getSenderId() == currentUserId && message.getReceiverId() == otherUserId) ||
                    (message.getSenderId() == otherUserId && message.getReceiverId() == currentUserId)) {
                adapter.addMessage(message);
                scrollToBottom();
            }
        });
    }

    private void sendMessage() {
        String text = etMessage.getText().toString().trim();
        if (text.isEmpty()) return;
        etMessage.setText("");

        if (otherUserId == 0) {
            chatApiService.checkAdminAvailable().enqueue(new Callback<AdminAvailableResponse>() {
                @Override
                public void onResponse(Call<AdminAvailableResponse> call,
                                       Response<AdminAvailableResponse> response) {
                    if (response.isSuccessful() && response.body() != null
                            && response.body().isAvailable()) {
                        stompChatService.sendMessage(currentUserId, otherUserId, text);
                    } else {
                        showAdminUnavailableDialog();
                    }
                }
                @Override
                public void onFailure(Call<AdminAvailableResponse> call, Throwable t) {
                    Log.e(TAG, "checkAdminAvailable failed", t);
                }
            });
        } else {
            stompChatService.sendMessage(currentUserId, otherUserId, text);
        }
    }

    private void showAdminUnavailableDialog() {
        if (!isAdded()) return;
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Support Unavailable")
                .setMessage("Sorry, there are no administrators available right now. Please try again later.")
                .setPositiveButton("OK", null)
                .show();
    }

    private void scrollToBottom() {
        if (getView() == null) return;
        RecyclerView rv = getView().findViewById(R.id.rvMessages);
        if (rv != null && adapter.getItemCount() > 0) {
            rv.smoothScrollToPosition(adapter.getItemCount() - 1);
        }
    }
}