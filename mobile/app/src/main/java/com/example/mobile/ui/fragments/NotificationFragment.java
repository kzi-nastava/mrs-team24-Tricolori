package com.example.mobile.ui.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mobile.R;
import com.example.mobile.dto.notification.NotificationDto;
import com.example.mobile.network.RetrofitClient;
import com.example.mobile.network.service.NotificationApiService;
import com.example.mobile.service.NotificationPushService;
import com.example.mobile.ui.adapters.NotificationAdapter;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NotificationFragment extends Fragment {

    private NotificationApiService apiService;
    private NotificationAdapter    adapter;

    private final List<NotificationDto> allNotifications = new ArrayList<>();
    private boolean showUnreadOnly = false;

    private RecyclerView   recyclerView;
    private View           loadingSpinner;
    private LinearLayout   emptyState;
    private TextView       tvUnreadCount;
    private TextView       tvReadCount;
    private TextView       tvTotalCount;
    private MaterialButton btnToggleFilter;
    private ImageButton    btnMarkAllRead;
    private ImageButton    btnClearAll;

    private LinearLayout        panicSection;
    private RecyclerView        panicRecycler;
    private NotificationAdapter panicAdapter;

    private boolean isAdmin = false;

    // Must use LocalBroadcastManager here — the service sends via
    // LocalBroadcastManager.sendBroadcast(), so this receiver must also
    // be registered through LocalBroadcastManager to receive it.
    private final BroadcastReceiver notificationReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String json = intent.getStringExtra(NotificationPushService.EXTRA_NOTIFICATION_JSON);
            if (json == null) return;
            NotificationDto dto = new Gson().fromJson(json, NotificationDto.class);
            if (dto != null) prependNotification(dto);
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_notification, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        apiService = RetrofitClient.getClient(requireContext())
                .create(NotificationApiService.class);

        SharedPreferences prefs = requireContext()
                .getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        isAdmin = "ROLE_ADMIN".equals(prefs.getString("user_role", ""));

        bindViews(view);
        setupAdapters();
        setupClickListeners();
        setupPanicSection();
        loadNotifications();
    }

    @Override
    public void onStart() {
        super.onStart();
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(
                notificationReceiver,
                new IntentFilter(NotificationPushService.ACTION_NEW_NOTIFICATION));
    }

    @Override
    public void onStop() {
        super.onStop();
        LocalBroadcastManager.getInstance(requireContext())
                .unregisterReceiver(notificationReceiver);
    }

    private void bindViews(View root) {
        recyclerView    = root.findViewById(R.id.rv_notifications);
        loadingSpinner  = root.findViewById(R.id.loading_spinner);
        emptyState      = root.findViewById(R.id.empty_state);
        tvUnreadCount   = root.findViewById(R.id.tv_unread_count);
        tvReadCount     = root.findViewById(R.id.tv_read_count);
        tvTotalCount    = root.findViewById(R.id.tv_total_count);
//        btnToggleFilter = root.findViewById(R.id.btn_toggle_filter);
        btnMarkAllRead  = root.findViewById(R.id.btn_mark_all_read);
        btnClearAll     = root.findViewById(R.id.btn_clear_all);
        panicSection    = root.findViewById(R.id.panic_section);
        panicRecycler   = root.findViewById(R.id.rv_panic_notifications);
    }

    private void setupAdapters() {
        adapter = new NotificationAdapter(this::openNotificationDetail);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);

        panicAdapter = new NotificationAdapter(this::openNotificationDetail);
        if (panicRecycler != null) {
            panicRecycler.setLayoutManager(new LinearLayoutManager(requireContext()));
            panicRecycler.setAdapter(panicAdapter);
        }
    }

    private void setupClickListeners() {
        btnToggleFilter.setOnClickListener(v -> {
            showUnreadOnly = !showUnreadOnly;
            btnToggleFilter.setText(showUnreadOnly ? "Show All" : "Unread Only");
            refreshList();
        });

        btnMarkAllRead.setOnClickListener(v -> markAllAsRead());
        btnClearAll.setOnClickListener(v -> confirmClearAll());
    }

    private void setupPanicSection() {
        if (panicSection != null) {
            panicSection.setVisibility(isAdmin ? View.VISIBLE : View.GONE);
        }
    }

    private void loadNotifications() {
        showLoading(true);

        apiService.getAllNotifications().enqueue(new Callback<List<NotificationDto>>() {
            @Override
            public void onResponse(@NonNull Call<List<NotificationDto>> call,
                                   @NonNull Response<List<NotificationDto>> response) {
                showLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    allNotifications.clear();
                    allNotifications.addAll(response.body());
                    refreshList();
                } else {
                    showToast("Failed to load notifications");
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<NotificationDto>> call,
                                  @NonNull Throwable t) {
                showLoading(false);
                showToast("Network error: " + t.getMessage());
            }
        });
    }

    private void refreshList() {
        List<NotificationDto> regularItems = allNotifications.stream()
                .filter(n -> !"RIDE_PANIC".equals(n.getType()))
                .filter(n -> !showUnreadOnly || !n.isOpened())
                .collect(Collectors.toList());

        List<NotificationDto> panicItems = allNotifications.stream()
                .filter(n -> "RIDE_PANIC".equals(n.getType()))
                .filter(n -> !showUnreadOnly || !n.isOpened())
                .collect(Collectors.toList());

        // Wrap in new ArrayList so DiffUtil sees a new list reference
        // and correctly diffs the contents instead of skipping the update.
        adapter.submitList(new ArrayList<>(regularItems));

        if (isAdmin && panicAdapter != null) {
            panicAdapter.submitList(new ArrayList<>(panicItems));
            if (panicSection != null) {
                panicSection.setVisibility(panicItems.isEmpty() ? View.GONE : View.VISIBLE);
            }
        }

        updateStats();
        updateEmptyState(regularItems.isEmpty() && panicItems.isEmpty());
    }

    private void updateStats() {
        long unread = allNotifications.stream().filter(n -> !n.isOpened()).count();
        long read   = allNotifications.stream().filter(n ->  n.isOpened()).count();
        long total  = allNotifications.size();

        tvUnreadCount.setText(String.valueOf(unread));
        tvReadCount.setText(String.valueOf(read));
        tvTotalCount.setText(total + " total");

        btnMarkAllRead.setVisibility(unread > 0 ? View.VISIBLE : View.GONE);
        btnClearAll.setVisibility(total > 0     ? View.VISIBLE : View.GONE);
    }

    private void updateEmptyState(boolean empty) {
        emptyState.setVisibility(empty ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(empty ? View.GONE   : View.VISIBLE);
    }

    private void prependNotification(NotificationDto dto) {
        allNotifications.add(0, dto);
        refreshList();
    }

    private void openNotificationDetail(NotificationDto dto) {
        if (!dto.isOpened()) markAsRead(dto);

        BottomSheetDialog sheet = new BottomSheetDialog(requireContext());
        View sheetView = LayoutInflater.from(requireContext())
                .inflate(R.layout.bottom_sheet_notification_detail, null);

        TextView tvSheetTitle   = sheetView.findViewById(R.id.tv_sheet_title);
        TextView tvSheetContent = sheetView.findViewById(R.id.tv_sheet_content);
        TextView tvSheetTime    = sheetView.findViewById(R.id.tv_sheet_time);
        TextView tvSheetRide    = sheetView.findViewById(R.id.tv_sheet_ride_id);
        TextView tvSheetDriver  = sheetView.findViewById(R.id.tv_sheet_driver);
        View     btnClose       = sheetView.findViewById(R.id.btn_sheet_close);

        tvSheetTitle.setText(getTitleForType(dto.getType()));
        tvSheetContent.setText(dto.getContent());
        tvSheetTime.setText(formatFullDate(dto.getTime()));

        if (dto.getRideId() != null) {
            tvSheetRide.setVisibility(View.VISIBLE);
            tvSheetRide.setText("Ride #" + dto.getRideId());
        } else {
            tvSheetRide.setVisibility(View.GONE);
        }

        if (dto.getDriverName() != null && !dto.getDriverName().isEmpty()) {
            tvSheetDriver.setVisibility(View.VISIBLE);
            tvSheetDriver.setText("Driver: " + dto.getDriverName());
        } else {
            tvSheetDriver.setVisibility(View.GONE);
        }

        btnClose.setOnClickListener(v -> sheet.dismiss());
        sheet.setContentView(sheetView);
        sheet.show();
    }

    private void markAsRead(NotificationDto dto) {
        // Optimistic update — reflect in UI immediately
        dto.setOpened(true);
        refreshList();

        apiService.markAsRead(dto.getId()).enqueue(new Callback<NotificationDto>() {
            @Override
            public void onResponse(@NonNull Call<NotificationDto> call,
                                   @NonNull Response<NotificationDto> response) {
                // Already updated — nothing to do
            }

            @Override
            public void onFailure(@NonNull Call<NotificationDto> call, @NonNull Throwable t) {
                // Roll back on failure
                dto.setOpened(false);
                refreshList();
            }
        });
    }

    private void markAllAsRead() {
        // Optimistic update — mark everything read instantly
        for (NotificationDto n : allNotifications) n.setOpened(true);
        refreshList();

        apiService.markAllAsRead().enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call,
                                   @NonNull Response<Void> response) {
                // Already updated — nothing to do
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                // Roll back by reloading from server
                showToast("Failed to mark all as read");
                loadNotifications();
            }
        });
    }

    private void confirmClearAll() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Clear All Notifications")
                .setMessage("Are you sure? You will lose all notification history.")
                .setPositiveButton("Clear All", (dialog, which) -> clearAll())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void clearAll() {
        // Optimistic update
        List<NotificationDto> backup = new ArrayList<>(allNotifications);
        allNotifications.clear();
        refreshList();

        apiService.deleteAllNotifications().enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call,
                                   @NonNull Response<Void> response) {
                // Already cleared — nothing to do
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                // Roll back
                showToast("Network error");
                allNotifications.addAll(backup);
                refreshList();
            }
        });
    }

    private void showLoading(boolean loading) {
        if (loadingSpinner != null)
            loadingSpinner.setVisibility(loading ? View.VISIBLE : View.GONE);
        if (recyclerView != null)
            recyclerView.setVisibility(loading ? View.GONE : View.VISIBLE);
    }

    private void showToast(String msg) {
        if (isAdded()) Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
    }

    private String getTitleForType(String type) {
        if (type == null) return "Notification";
        switch (type) {
            case "RIDE_PANIC":             return "🚨 Emergency Panic Alert";
            case "RIDE_STARTING":          return "Ride is starting";
            case "RIDE_STARTED":           return "Ride has started";
            case "RIDE_COMPLETED":         return "Ride completed";
            case "RIDE_CANCELLED":         return "Ride cancelled";
            case "RIDE_REJECTED":          return "Ride request rejected";
            case "ADDED_TO_RIDE":          return "Added to shared ride";
            case "RATING_REMINDER":        return "Rating reminder";
            case "RATING_RECEIVED":        return "You received a rating";
            case "RIDE_REMINDER":
            case "UPCOMING_RIDE_REMINDER": return "Upcoming ride reminder";
            case "RIDE_REPORT":            return "Ride issue reported";
            case "NEW_REGISTRATION":       return "New driver registered";
            case "PROFILE_CHANGE_REQUEST": return "Profile change request";
            case "NEW_CHAT_MESSAGE":       return "New support message";
            default:                       return "Notification";
        }
    }

    private String formatFullDate(String isoTime) {
        if (isoTime == null) return "";
        try {
            java.time.Instant instant = java.time.Instant.parse(isoTime);
            java.time.ZonedDateTime zdt = instant.atZone(java.time.ZoneId.systemDefault());
            return java.time.format.DateTimeFormatter
                    .ofPattern("EEEE, MMMM d yyyy  HH:mm")
                    .format(zdt);
        } catch (Exception e) {
            return isoTime;
        }
    }
}