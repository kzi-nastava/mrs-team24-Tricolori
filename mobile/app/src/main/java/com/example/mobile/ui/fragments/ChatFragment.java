package com.example.mobile.ui.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mobile.R;
import com.example.mobile.dto.chat.AdminAvailableResponse;
import com.example.mobile.dto.chat.AdminIdResponse;
import com.example.mobile.dto.chat.ChatMessageDto;
import com.example.mobile.network.RetrofitClient;
import com.example.mobile.network.service.ChatApiService;
import com.example.mobile.network.service.StompChatService;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

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

    private RecyclerView recyclerView;
    private ChatAdapter adapter;
    private EditText etMessage;
    private ImageButton btnSend;
    private TextView tvOtherName;

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

        recyclerView = view.findViewById(R.id.rvMessages);
        etMessage    = view.findViewById(R.id.etMessage);
        btnSend      = view.findViewById(R.id.btnSend);
        tvOtherName  = view.findViewById(R.id.tvOtherName);
        view.findViewById(R.id.btnBack).setOnClickListener(v ->
                requireActivity().getSupportFragmentManager().popBackStack());

        tvOtherName.setText(otherUserName);

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
                .enqueue(new Callback<List<ChatMessageDTO>>() {
                    @Override
                    public void onResponse(Call<List<ChatMessageDTO>> call,
                                           Response<List<ChatMessageDTO>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            adapter.setMessages(response.body());
                            scrollToBottom();
                        }
                    }
                    @Override
                    public void onFailure(Call<List<ChatMessageDTO>> call, Throwable t) {
                        Log.e(TAG, "loadHistory failed", t);
                    }
                });
    }

    private void connectWebSocket() {
        stompChatService = new StompChatService();
        stompChatService.connect(currentUserId, message -> {
            if (!isAdded()) return;
            if ((message.getSenderId() == currentUserId && message.getReceiverId() == otherUserId) ||
                    (message.getSenderId() == otherUserId   && message.getReceiverId() == currentUserId)) {
                adapter.addMessage(message);
                scrollToBottom();
            }
        });
    }

    private void sendMessage() {
        String text = etMessage.getText().toString().trim();
        if (text.isEmpty()) return;
        etMessage.setText("");

        // For user→admin flow: check admin available first
        // If otherUserId is already known (admin flow from list), skip the check
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
        if (adapter.getItemCount() > 0) {
            recyclerView.smoothScrollToPosition(adapter.getItemCount() - 1);
        }
    }
}