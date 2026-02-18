package com.example.mobile.ui.fragments;

import android.content.SharedPreferences;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mobile.R;
import com.example.mobile.dto.chat.ChatUserDto;
import com.example.mobile.network.RetrofitClient;
import com.example.mobile.network.service.ChatApiService;
import com.example.mobile.ui.adapters.ConversationAdapter;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminConversationListFragment extends Fragment {

    private static final String TAG = "AdminChatList";
    private static final long POLL_INTERVAL_MS = 10_000L;

    private RecyclerView recyclerView;
    private ConversationAdapter adapter;
    private TextView tvEmpty;
    private ChatApiService chatApiService;
    private long adminId;
    private final Handler pollHandler = new Handler(Looper.getMainLooper());
    private Runnable pollRunnable;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_admin_conversation_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = view.findViewById(R.id.rvConversations);
        tvEmpty      = view.findViewById(R.id.tvEmpty);

        SharedPreferences prefs = requireContext()
                .getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        adminId = prefs.getLong("user_id", 0L);

        chatApiService = RetrofitClient.getClient(requireContext()).create(ChatApiService.class);

        adapter = new ConversationAdapter(user -> {
            String name = user.getFirstName() + " " + user.getLastName();
            ChatFragment chat = ChatFragment.newInstance(adminId, user.getId(), name);
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.nav_host_fragment, chat)
                    .addToBackStack(null)
                    .commit();
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);

        loadConversations();
        startPolling();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        stopPolling();
    }

    private void loadConversations() {
        chatApiService.getActiveChats(adminId).enqueue(new Callback<List<ChatUserDto>>() {
            @Override
            public void onResponse(Call<List<ChatUserDto>> call,
                                   Response<List<ChatUserDto>> response) {
                if (!isAdded()) return;
                if (response.isSuccessful() && response.body() != null) {
                    List<ChatUserDto> list = response.body();
                    adapter.setUsers(list);
                    tvEmpty.setVisibility(list.isEmpty() ? View.VISIBLE : View.GONE);
                }
            }
            @Override
            public void onFailure(Call<List<ChatUserDto>> call, Throwable t) {
                Log.e(TAG, "loadConversations failed", t);
            }
        });
    }

    private void startPolling() {
        pollRunnable = new Runnable() {
            @Override
            public void run() {
                loadConversations();
                pollHandler.postDelayed(this, POLL_INTERVAL_MS);
            }
        };
        pollHandler.postDelayed(pollRunnable, POLL_INTERVAL_MS);
    }

    private void stopPolling() {
        pollHandler.removeCallbacks(pollRunnable);
    }
}