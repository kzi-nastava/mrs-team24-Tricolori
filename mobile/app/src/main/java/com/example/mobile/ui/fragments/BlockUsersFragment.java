package com.example.mobile.ui.fragments;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mobile.R;
import com.example.mobile.dto.PageResponse;
import com.example.mobile.dto.block.ActivePersonStatus;
import com.example.mobile.dto.block.BlockRequest;
import com.example.mobile.enums.AccountStatus;
import com.example.mobile.network.RetrofitClient;
import com.example.mobile.ui.adapters.ActivePersonStatusAdapter;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BlockUsersFragment extends Fragment implements ActivePersonStatusAdapter.OnUserActionListener {

    // ─── State ───────────────────────────────────────────────────────────────
    private int currentPage = 0;
    private final int pageSize = 5;
    private long totalElements = 0;

    // ─── Views ───────────────────────────────────────────────────────────────
    private EditText etFilterId, etFilterFirstName, etFilterLastName, etFilterEmail;
    private Button btnApplyFilters, btnPrev, btnNext;
    private TextView btnResetFilters, tvDisplayRange, tvCurrentPage, tvTotalPages;
    private RecyclerView rvUsers;
    private LinearLayout llEmptyState;

    // ─── Adapter ─────────────────────────────────────────────────────────────
    private ActivePersonStatusAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_block_users, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        bindViews(view);
        setupRecyclerView();
        setupClickListeners();
        loadUsers();
    }

    // ─── View binding ────────────────────────────────────────────────────────

    private void bindViews(View view) {
        etFilterId        = view.findViewById(R.id.etFilterId);
        etFilterFirstName = view.findViewById(R.id.etFilterFirstName);
        etFilterLastName  = view.findViewById(R.id.etFilterLastName);
        etFilterEmail     = view.findViewById(R.id.etFilterEmail);
        btnResetFilters   = view.findViewById(R.id.btnResetFilters);
        btnApplyFilters   = view.findViewById(R.id.btnApplyFilters);
        rvUsers           = view.findViewById(R.id.rvUsers);
        llEmptyState      = view.findViewById(R.id.llEmptyState);
        tvDisplayRange    = view.findViewById(R.id.tvDisplayRange);
        tvCurrentPage     = view.findViewById(R.id.tvCurrentPage);
        tvTotalPages      = view.findViewById(R.id.tvTotalPages);
        btnPrev           = view.findViewById(R.id.btnPrev);
        btnNext           = view.findViewById(R.id.btnNext);
    }

    private void setupRecyclerView() {
        adapter = new ActivePersonStatusAdapter(this);
        rvUsers.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvUsers.setAdapter(adapter);
    }

    private void setupClickListeners() {
        btnApplyFilters.setOnClickListener(v -> {
            currentPage = 0;
            loadUsers();
        });

        btnResetFilters.setOnClickListener(v -> {
            etFilterId.setText("");
            etFilterFirstName.setText("");
            etFilterLastName.setText("");
            etFilterEmail.setText("");
            currentPage = 0;
            loadUsers();
        });

        btnPrev.setOnClickListener(v -> {
            if (currentPage > 0) {
                currentPage--;
                loadUsers();
            }
        });

        btnNext.setOnClickListener(v -> {
            int totalPages = (int) Math.ceil((double) totalElements / pageSize);
            if ((currentPage + 1) < totalPages) {
                currentPage++;
                loadUsers();
            }
        });
    }

    // ─── API calls ───────────────────────────────────────────────────────────

    private void loadUsers() {
        String idStr    = etFilterId.getText().toString().trim();
        String fName    = etFilterFirstName.getText().toString().trim();
        String lName    = etFilterLastName.getText().toString().trim();
        String email    = etFilterEmail.getText().toString().trim();

        Long id = null;
        if (!idStr.isEmpty()) {
            try { id = Long.parseLong(idStr); } catch (NumberFormatException ignored) {}
        }

        RetrofitClient.getPersonService(requireContext()).getUsers(
                id,
                fName.isEmpty()  ? null : fName,
                lName.isEmpty()  ? null : lName,
                email.isEmpty()  ? null : email,
                currentPage,
                pageSize
        ).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<PageResponse<ActivePersonStatus>> call,
                                   @NonNull Response<PageResponse<ActivePersonStatus>> response) {
                if (!isAdded()) return;
                if (response.isSuccessful() && response.body() != null) {
                    PageResponse<ActivePersonStatus> page = response.body();
                    totalElements = page.getTotalElements();
                    adapter.setUsers(page.getContent());
                    updatePaginationUI();
                    updateEmptyState(page.getContent().isEmpty());
                } else {
                    showError("Failed to load users.");
                }
            }

            @Override
            public void onFailure(@NonNull Call<PageResponse<ActivePersonStatus>> call,
                                  @NonNull Throwable t) {
                if (!isAdded()) return;
                showError("Network error: " + t.getMessage());
            }
        });
    }

    // ─── Called by adapter ───────────────────────────────────────────────────

    @Override
    public void onChangeStatus(ActivePersonStatus user) {
        showStatusDialog(user);
    }

    // ─── Dialog ──────────────────────────────────────────────────────────────

    private void showStatusDialog(ActivePersonStatus user) {
        Dialog dialog = new Dialog(requireContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_block_user);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setLayout(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        boolean isSuspended = user.getStatus() == AccountStatus.SUSPENDED;

        TextView tvTitle        = dialog.findViewById(R.id.tvDialogTitle);
        TextView tvEmail        = dialog.findViewById(R.id.tvDialogEmail);
        LinearLayout llReason  = dialog.findViewById(R.id.llBlockReason);
        TextView tvUnblock     = dialog.findViewById(R.id.tvUnblockConfirm);
        EditText etReason      = dialog.findViewById(R.id.etBlockReason);
        TextView btnCancel     = dialog.findViewById(R.id.btnDialogCancel);
        Button btnConfirm      = dialog.findViewById(R.id.btnDialogConfirm);

        tvTitle.setText(isSuspended ?
                R.string.block_result_item_change_status_unblock :
                R.string.block_result_item_change_status_block);
        tvEmail.setText(user.getEmail());

        if (isSuspended) {
            llReason.setVisibility(View.GONE);
            tvUnblock.setVisibility(View.VISIBLE);
            btnConfirm.setText("Confirm unblock");

            setButtonColor(btnConfirm, getHexColor(R.color.light_600));
        } else {
            llReason.setVisibility(View.VISIBLE);
            tvUnblock.setVisibility(View.GONE);
            btnConfirm.setText("Block now");
            setButtonColor(btnConfirm, getHexColor(R.color.base_600));
        }

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnConfirm.setOnClickListener(v -> {
            if (isSuspended) {
                removeBlock(user.getEmail(), dialog);
            } else {
                String reason = etReason.getText().toString().trim();
                if (TextUtils.isEmpty(reason)) {
                    etReason.setError("Block reason is mandatory");
                    return;
                }
                applyBlock(new BlockRequest(reason, user.getEmail()), dialog);
            }
        });

        dialog.show();
    }

    private void applyBlock(BlockRequest request, Dialog dialog) {
        RetrofitClient.getPersonService(requireContext()).applyBlock(request).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                if (!isAdded()) return;
                dialog.dismiss();
                if (response.isSuccessful()) {
                    loadUsers();
                    Toast.makeText(requireContext(), "User blocked successfully.", Toast.LENGTH_SHORT).show();
                } else {
                    showError("Failed to block user.");
                }
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                if (!isAdded()) return;
                dialog.dismiss();
                showError("Network error: " + t.getMessage());
            }
        });
    }

    private void removeBlock(String email, Dialog dialog) {
        RetrofitClient.getPersonService(requireContext()).removeBlock(email).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                if (!isAdded()) return;
                dialog.dismiss();
                if (response.isSuccessful()) {
                    loadUsers();
                    Toast.makeText(requireContext(), "User unblocked successfully.", Toast.LENGTH_SHORT).show();
                } else {
                    showError("Failed to unblock user.");
                }
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                if (!isAdded()) return;
                dialog.dismiss();
                showError("Network error: " + t.getMessage());
            }
        });
    }

    // ─── UI helpers ──────────────────────────────────────────────────────────

    private void updatePaginationUI() {
        int totalPages = (int) Math.ceil((double) totalElements / pageSize);

        tvCurrentPage.setText(String.valueOf(currentPage + 1));
        tvTotalPages.setText("of " + Math.max(totalPages, 1));

        btnPrev.setEnabled(currentPage > 0);
        btnPrev.setAlpha(currentPage > 0 ? 1f : 0.4f);

        btnNext.setEnabled((currentPage + 1) < totalPages);
        btnNext.setAlpha((currentPage + 1) < totalPages ? 1f : 0.4f);

        if (totalElements == 0) {
            tvDisplayRange.setText("No users to show");
        } else {
            int start = currentPage * pageSize + 1;
            int end = (int) Math.min((long)(currentPage + 1) * pageSize, totalElements);
            tvDisplayRange.setText("Showing " + start + "–" + end + " out of " + totalElements + " users");
        }
    }

    private void updateEmptyState(boolean isEmpty) {
        llEmptyState.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        rvUsers.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
    }

    private void setButtonColor(Button button, String hexColor) {
        GradientDrawable bg = new GradientDrawable();
        bg.setShape(GradientDrawable.RECTANGLE);
        bg.setCornerRadius(16f);
        bg.setColor(Color.parseColor(hexColor));
        button.setBackground(bg);
    }

    private void showError(String message) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show();
    }

    private String getHexColor(int colorResId) {
        int color = ContextCompat.getColor(getContext(), colorResId);
        return String.format("#%06X", (0xFFFFFF & color));
    }
}