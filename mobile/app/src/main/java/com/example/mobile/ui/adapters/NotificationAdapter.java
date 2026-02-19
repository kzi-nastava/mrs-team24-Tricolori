package com.example.mobile.ui.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mobile.R;
import com.example.mobile.dto.notification.NotificationDto;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {

    public interface OnNotificationClickListener {
        void onNotificationClicked(NotificationDto notification);
    }

    private List<NotificationDto> items = new ArrayList<>();
    private final OnNotificationClickListener clickListener;

    public NotificationAdapter(OnNotificationClickListener clickListener) {
        this.clickListener = clickListener;
    }


    public void submitList(List<NotificationDto> newList) {
        DiffUtil.DiffResult result = DiffUtil.calculateDiff(new DiffUtil.Callback() {
            @Override public int getOldListSize() { return items.size(); }
            @Override public int getNewListSize() { return newList.size(); }

            @Override
            public boolean areItemsTheSame(int oldPos, int newPos) {
                return items.get(oldPos).getId() == newList.get(newPos).getId();
            }

            @Override
            public boolean areContentsTheSame(int oldPos, int newPos) {
                NotificationDto o = items.get(oldPos);
                NotificationDto n = newList.get(newPos);
                return o.isOpened() == n.isOpened();
            }
        });

        items = new ArrayList<>(newList);
        result.dispatchUpdatesTo(this);
    }


    @Override
    public int getItemViewType(int position) {
        // Panic items get a distinct view type so they can be styled differently
        return "RIDE_PANIC".equals(items.get(position).getType()) ? 1 : 0;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int layout = viewType == 1
                ? R.layout.item_notification_panic
                : R.layout.item_notification;
        View v = LayoutInflater.from(parent.getContext()).inflate(layout, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() { return items.size(); }

    class ViewHolder extends RecyclerView.ViewHolder {

        private final TextView tvTitle;
        private final TextView tvContent;
        private final TextView tvTime;
        private final TextView tvRideId;
        private final ImageView ivIcon;
        private final View     unreadDot;
        private final View     unreadStripe; // left coloured stripe for unread

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle      = itemView.findViewById(R.id.tv_notif_title);
            tvContent    = itemView.findViewById(R.id.tv_notif_content);
            tvTime       = itemView.findViewById(R.id.tv_notif_time);
            tvRideId     = itemView.findViewById(R.id.tv_notif_ride_id);
            ivIcon       = itemView.findViewById(R.id.iv_notif_icon);
            unreadDot    = itemView.findViewById(R.id.view_unread_dot);
            unreadStripe = itemView.findViewById(R.id.view_unread_stripe);
        }

        void bind(NotificationDto dto) {
            Context ctx = itemView.getContext();

            tvTitle.setText(getTitleForType(dto.getType()));
            tvContent.setText(dto.getContent());
            tvTime.setText(formatTimeAgo(dto.getTime()));

            // Ride ID badge
            if (dto.getRideId() != null) {
                tvRideId.setVisibility(View.VISIBLE);
                tvRideId.setText(ctx.getString(R.string.ride_id_label, dto.getRideId()));
            } else {
                tvRideId.setVisibility(View.GONE);
            }

            // Icon
            ivIcon.setImageResource(getIconForType(dto.getType()));
            ivIcon.setBackgroundTintList(ContextCompat.getColorStateList(ctx, getIconBgColorForType(dto.getType())));

            // Unread state
            boolean unread = !dto.isOpened();
            if (unreadDot    != null) unreadDot.setVisibility(unread ? View.VISIBLE : View.GONE);
            if (unreadStripe != null) unreadStripe.setVisibility(unread ? View.VISIBLE : View.GONE);
            itemView.setAlpha(unread ? 1.0f : 0.75f);

            itemView.setOnClickListener(v -> {
                if (clickListener != null) clickListener.onNotificationClicked(dto);
            });
        }
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

    private int getIconForType(String type) {
        if (type == null) return R.drawable.ic_notification;
        switch (type) {
            case "RIDE_PANIC":              return R.drawable.ic_warning;
            case "RIDE_STARTING":
            case "RIDE_STARTED":
            case "RIDE_REMINDER":
            case "UPCOMING_RIDE_REMINDER":  return R.drawable.ic_time;
            case "RIDE_CANCELLED":
            case "RIDE_REJECTED":           return R.drawable.ic_close;
            case "RIDE_COMPLETED":          return R.drawable.ic_check_circle;
            case "RATING_REMINDER":
            case "RATING_RECEIVED":         return R.drawable.ic_star_empty;
            case "ADDED_TO_RIDE":           return R.drawable.ic_person;
            case "NEW_CHAT_MESSAGE":        return R.drawable.ic_support;
            case "NEW_REGISTRATION":        return R.drawable.ic_vehicle;
            case "PROFILE_CHANGE_REQUEST":  return R.drawable.ic_change_requests;
            default:                        return R.drawable.ic_notification;
        }
    }

    private int getIconBgColorForType(String type) {
        if (type == null) return R.color.notif_bg_gray;
        switch (type) {
            case "RIDE_PANIC":              return R.color.notif_bg_red;
            case "RIDE_STARTING":
            case "RIDE_STARTED":
            case "RIDE_REMINDER":
            case "UPCOMING_RIDE_REMINDER":  return R.color.notif_bg_blue;
            case "RIDE_CANCELLED":
            case "RIDE_REJECTED":           return R.color.notif_bg_red;
            case "RIDE_COMPLETED":          return R.color.notif_bg_green;
            case "RATING_REMINDER":
            case "RATING_RECEIVED":         return R.color.notif_bg_amber;
            case "ADDED_TO_RIDE":           return R.color.notif_bg_purple;
            case "NEW_CHAT_MESSAGE":        return R.color.notif_bg_indigo;
            case "NEW_REGISTRATION":        return R.color.notif_bg_green;
            case "PROFILE_CHANGE_REQUEST":  return R.color.notif_bg_sky;
            default:                        return R.color.notif_bg_gray;
        }
    }

    private String formatTimeAgo(String isoTime) {
        if (isoTime == null) return "";
        try {
            Instant instant = Instant.parse(isoTime);
            long diffMs = System.currentTimeMillis() - instant.toEpochMilli();
            long mins  = diffMs / 60_000;
            long hours = diffMs / 3_600_000;
            long days  = diffMs / 86_400_000;

            if (mins  < 1)  return "Just now";
            if (mins  < 60) return mins + "m ago";
            if (hours < 24) return hours + "h ago";
            if (days  < 7)  return days  + "d ago";

            ZonedDateTime zdt = instant.atZone(ZoneId.systemDefault());
            return DateTimeFormatter.ofPattern("MMM d").format(zdt);
        } catch (Exception e) {
            return isoTime;
        }
    }
}