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
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mobile.R;
import com.example.mobile.dto.notification.NotificationDto;
import com.example.mobile.network.RetrofitClient;
import com.example.mobile.network.service.NotificationApiService;
import com.example.mobile.service.NotificationPushService;
import com.example.mobile.ui.adapters.NotificationAdapter;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Displays the user's notification history and receives real-time updates
 * via a local broadcast from {@link NotificationPushService}.
 */
public class NotificationFragment extends Fragment {

    private NotificationApiService apiService;
    private NotificationAdapter   adapter;

    // All notifications currently loaded
    private final List<NotificationDto> allNotifications = new ArrayList<>();
    private boolean showUnreadOnly = false;

    // UI refs
    private RecyclerView recyclerView;
    private View          loadingSpinner;
    private LinearLayout  emptyState;
    private TextView      tvUnreadCount;
    private TextView      tvReadCount;
    private TextView      tvTotalCount;
    private Button        btnToggleFilter;
    private Button        btnMarkAllRead;
    private Button        btnClearAll;

    // Admin-only panic section
    private LinearLayout  panicSection;
    private RecyclerView  panicRecycler;
    private NotificationAdapter panicAdapter;

    private boolean isAdmin = false;

    // ─────────────────────────────────────────────────────────────────
    //  BroadcastReceiver — listens for real-time pushes from the service
    // ─────────────────────────────────────────────────────────────────

    private final BroadcastReceiver notificationReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String json = intent.getStringExtra("notification_json");
            if (json == null) return;
            NotificationDto dto = new Gson().fromJson(json, NotificationDto.class);
            if (dto != null) prependNotification(dto);
        }
    };

    // ─────────────────────────────────────────────────────────────────
    //  Fragment lifecycle
    // ─────────────────────────────────────────────────────────────────

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
        // Register broadcast receiver for live updates
        IntentFilter filter = new IntentFilter("com.example.mobile.NEW_NOTIFICATION");
        ContextCompat.registerReceiver(
                requireContext(),
                notificationReceiver,
                filter,
                ContextCompat.RECEIVER_NOT_EXPORTED
        );
    }

    @Override
    public void onStop() {
        super.onStop();
        requireContext().unregisterReceiver(notificationReceiver);
    }

    // ─────────────────────────────────────────────────────────────────
    //  View binding
    // ─────────────────────────────────────────────────────────────────

    private void bindViews(View root) {
        recyclerView    = root.findViewById(R.id.rv_notifications);
        loadingSpinner  = root.findViewById(R.id.loading_spinner);
        emptyState      = root.findViewById(R.id.empty_state);
        tvUnreadCount   = root.findViewById(R.id.tv_unread_count);
        tvReadCount     = root.findViewById(R.id.tv_read_count);
        tvTotalCount    = root.findViewById(R.id.tv_total_count);
        btnToggleFilter = root.findViewById(R.id.btn_toggle_filter);
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
            // Only admins see the dedicated panic section at the top
            panicSection.setVisibility(isAdmin ? View.VISIBLE : View.GONE);
        }
    }

    // ─────────────────────────────────────────────────────────────────
    //  Data loading
    // ─────────────────────────────────────────────────────────────────

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

    // ─────────────────────────────────────────────────────────────────
    //  List refresh helpers
    // ─────────────────────────────────────────────────────────────────

    private void refreshList() {
        List<NotificationDto> regularItems = allNotifications.stream()
                .filter(n -> !"RIDE_PANIC".equals(n.getType()))
                .filter(n -> !showUnreadOnly || !n.isOpened())
                .collect(Collectors.toList());

        List<NotificationDto> panicItems = allNotifications.stream()
                .filter(n -> "RIDE_PANIC".equals(n.getType()))
                .filter(n -> !showUnreadOnly || !n.isOpened())
                .collect(Collectors.toList());

        adapter.submitList(regularItems);

        if (isAdmin && panicAdapter != null) {
            panicAdapter.submitList(panicItems);
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
        btnClearAll.setVisibility(total > 0    ? View.VISIBLE : View.GONE);
    }

    private void updateEmptyState(boolean empty) {
        emptyState.setVisibility(empty ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(empty ? View.GONE  : View.VISIBLE);
    }

    /** Prepends a freshly received real-time notification to the top of the list */
    private void prependNotification(NotificationDto dto) {
        allNotifications.add(0, dto);
        refreshList();
    }

    // ─────────────────────────────────────────────────────────────────
    //  Notification detail (bottom sheet)
    // ─────────────────────────────────────────────────────────────────

    private void openNotificationDetail(NotificationDto dto) {
        // Mark as read on the server if still unread
        if (!dto.isOpened()) {
            markAsRead(dto);
        }

        BottomSheetDialog sheet = new BottomSheetDialog(requireContext());
        View sheetView = LayoutInflater.from(requireContext())
                .inflate(R.layout.bottom_sheet_notification_detail, null);

        TextView tvSheetTitle   = sheetView.findViewById(R.id.tv_sheet_title);
        TextView tvSheetContent = sheetView.findViewById(R.id.tv_sheet_content);
        TextView tvSheetTime    = sheetView.findViewById(R.id.tv_sheet_time);
        TextView tvSheetRide    = sheetView.findViewById(R.id.tv_sheet_ride_id);
        TextView tvSheetDriver  = sheetView.findViewById(R.id.tv_sheet_driver);
        Button   btnClose       = sheetView.findViewById(R.id.btn_sheet_close);

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

    // ─────────────────────────────────────────────────────────────────
    //  API actions
    // ─────────────────────────────────────────────────────────────────

    private void markAsRead(NotificationDto dto) {
        apiService.markAsRead(dto.getId()).enqueue(new Callback<NotificationDto>() {
            @Override
            public void onResponse(@NonNull Call<NotificationDto> call,
                                   @NonNull Response<NotificationDto> response) {
                if (response.isSuccessful()) {
                    dto.setOpened(true);
                    refreshList();
                }
            }
            @Override
            public void onFailure(@NonNull Call<NotificationDto> call, @NonNull Throwable t) {
                // Non-critical — swallow silently
            }
        });
    }

    private void markAllAsRead() {
        apiService.markAllAsRead().enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call,
                                   @NonNull Response<Void> response) {
                if (response.isSuccessful()) {
                    for (NotificationDto n : allNotifications) n.setOpened(true);
                    refreshList();
                }
            }
            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                showToast("Network error");
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
        apiService.deleteAllNotifications().enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call,
                                   @NonNull Response<Void> response) {
                if (response.isSuccessful()) {
                    allNotifications.clear();
                    refreshList();
                }
            }
            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                showToast("Network error");
            }
        });
    }

    // ─────────────────────────────────────────────────────────────────
    //  UI helpers
    // ─────────────────────────────────────────────────────────────────

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
            case "RIDE_PANIC":              return "🚨 Emergency Panic Alert";
            case "RIDE_STARTING":           return "Ride is starting";
            case "RIDE_STARTED":            return "Ride has started";
            case "RIDE_COMPLETED":          return "Ride completed";
            case "RIDE_CANCELLED":          return "Ride cancelled";
            case "RIDE_REJECTED":           return "Ride request rejected";
            case "ADDED_TO_RIDE":           return "Added to shared ride";
            case "RATING_REMINDER":         return "Rating reminder";
            case "RATING_RECEIVED":         return "You received a rating";
            case "RIDE_REMINDER":
            case "UPCOMING_RIDE_REMINDER":  return "Upcoming ride reminder";
            case "RIDE_REPORT":             return "Ride issue reported";
            case "NEW_REGISTRATION":        return "New driver registered";
            case "PROFILE_CHANGE_REQUEST":  return "Profile change request";
            case "NEW_CHAT_MESSAGE":        return "New support message";
            default:                        return "Notification";
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