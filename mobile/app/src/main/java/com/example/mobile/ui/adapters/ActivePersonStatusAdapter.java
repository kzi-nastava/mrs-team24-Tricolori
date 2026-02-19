package com.example.mobile.ui.adapters;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mobile.R;
import com.example.mobile.dto.block.ActivePersonStatus;
import com.example.mobile.enums.AccountStatus;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ActivePersonStatusAdapter extends RecyclerView.Adapter<ActivePersonStatusAdapter.UserViewHolder> {

    public interface OnUserActionListener {
        void onChangeStatus(ActivePersonStatus user);
    }

    private List<ActivePersonStatus> users = new ArrayList<>();
    private final OnUserActionListener listener;

    public ActivePersonStatusAdapter(OnUserActionListener listener) {
        this.listener = listener;
    }

    public void setUsers(List<ActivePersonStatus> users) {
        this.users = users;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_user, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        holder.bind(users.get(position));
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    class UserViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvUserId;
        private final TextView tvStatus;
        private final TextView tvFullName;
        private final TextView tvEmail;
        private final TextView tvRegistrationDate;
        private final Button btnChangeStatus;

        UserViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUserId           = itemView.findViewById(R.id.tvUserId);
            tvStatus           = itemView.findViewById(R.id.tvStatus);
            tvFullName         = itemView.findViewById(R.id.tvFullName);
            tvEmail            = itemView.findViewById(R.id.tvEmail);
            tvRegistrationDate = itemView.findViewById(R.id.tvRegistrationDate);
            btnChangeStatus    = itemView.findViewById(R.id.btnChangeStatus);
        }

        void bind(ActivePersonStatus user) {
            tvUserId.setText("#" + user.getId());
            tvFullName.setText(user.getFirstName() + " " + user.getLastName());
            tvEmail.setText(user.getEmail());
            tvRegistrationDate.setText(formatDate(user.getRegistrationDate()));

            // Status badge
            tvStatus.setText(user.getStatus().name());
            int statusColor = getStatusColor(user.getStatus());
            GradientDrawable badgeBg = new GradientDrawable();
            badgeBg.setShape(GradientDrawable.RECTANGLE);
            badgeBg.setCornerRadius(100f);
            badgeBg.setColor(statusColor);
            tvStatus.setBackground(badgeBg);

            // Action button
            Context context = btnChangeStatus.getContext();
            GradientDrawable btnBg = new GradientDrawable();
            btnBg.setShape(GradientDrawable.RECTANGLE);
            btnBg.setCornerRadius(dpToPx(8));

            if (user.getStatus() == AccountStatus.SUSPENDED) {
                btnChangeStatus.setText(context.getString(R.string.block_result_item_change_status_unblock));
                int colorEmerald = ContextCompat.getColor(context, R.color.light_600);
                btnBg.setColor(colorEmerald);
            } else {
                btnChangeStatus.setText(context.getString(R.string.block_result_item_change_status_block));
                int colorBase = ContextCompat.getColor(context, R.color.base_600);
                btnBg.setColor(colorBase);
            }
            btnChangeStatus.setBackground(btnBg);
            btnChangeStatus.setOnClickListener(v -> listener.onChangeStatus(user));
        }

        private int getStatusColor(AccountStatus status) {
            switch (status) {
                case ACTIVE:
                    return Color.parseColor("#10B981"); // emerald-500
                case WAITING_FOR_ACTIVATION:
                    return Color.parseColor("#F59E0B"); // amber-500
                case SUSPENDED:
                    return Color.parseColor("#EF4444"); // red-500
                default:
                    return Color.GRAY;
            }
        }

        private String formatDate(String isoDate) {
            // Expects ISO 8601 or similar; basic trim for display
            // Example: "2024-01-15T14:30:00" → "15.01.2024 - 14:30"
            if (isoDate == null || isoDate.isEmpty()) return "-";
            try {
                // Parse ISO date: yyyy-MM-dd'T'HH:mm:ss
                SimpleDateFormat inputFmt = new SimpleDateFormat(
                        "yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
                SimpleDateFormat outputFmt = new SimpleDateFormat(
                        "dd.MM.yyyy - HH:mm", Locale.getDefault());
                Date date = inputFmt.parse(isoDate);
                return date != null ? outputFmt.format(date) : isoDate;
            } catch (Exception e) {
                return isoDate;
            }
        }

        private float dpToPx(int dp) {
            return dp * itemView.getContext().getResources().getDisplayMetrics().density;
        }
    }
}
