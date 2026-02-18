package com.example.mobile.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mobile.R;
import com.example.mobile.dto.chat.ChatUserDto;

import java.util.ArrayList;
import java.util.List;

public class ConversationAdapter extends RecyclerView.Adapter<ConversationAdapter.ConvViewHolder> {

    public interface OnUserClickListener {
        void onUserClick(ChatUserDto user);
    }

    private final List<ChatUserDto> users = new ArrayList<>();
    private final OnUserClickListener listener;

    public ConversationAdapter(OnUserClickListener listener) {
        this.listener = listener;
    }

    public void setUsers(List<ChatUserDto> newUsers) {
        users.clear();
        users.addAll(newUsers);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ConvViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_conversation, parent, false);
        return new ConvViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ConvViewHolder holder, int position) {
        holder.bind(users.get(position), listener);
    }

    @Override
    public int getItemCount() { return users.size(); }

    static class ConvViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvRole, tvLastMessage, tvTime, tvUnreadBadge, tvAvatar;

        ConvViewHolder(View itemView) {
            super(itemView);
            tvName        = itemView.findViewById(R.id.tvUserName);
            tvRole        = itemView.findViewById(R.id.tvUserRole);
            tvLastMessage = itemView.findViewById(R.id.tvLastMessage);
            tvTime        = itemView.findViewById(R.id.tvLastMessageTime);
            tvUnreadBadge = itemView.findViewById(R.id.tvUnreadBadge);
            tvAvatar      = itemView.findViewById(R.id.tvAvatarLetter);
        }

        void bind(ChatUserDto user, OnUserClickListener listener) {
            String fullName = user.getFirstName() + " " + user.getLastName();
            tvName.setText(fullName);
            tvRole.setText(formatRole(user.getRole()));
            tvLastMessage.setText(user.getLastMessage() != null ? user.getLastMessage() : "No messages yet");
            tvTime.setText(user.getLastMessageTime() != null ? user.getLastMessageTime() : "");
            tvUnreadBadge.setVisibility(user.isHasUnread() ? View.VISIBLE : View.GONE);
            tvAvatar.setText(user.getFirstName() != null && !user.getFirstName().isEmpty()
                    ? String.valueOf(user.getFirstName().charAt(0)).toUpperCase() : "?");
            itemView.setOnClickListener(v -> listener.onUserClick(user));
        }

        private String formatRole(String role) {
            if (role == null) return "";
            switch (role) {
                case "ROLE_PASSENGER": return "Passenger";
                case "ROLE_DRIVER":    return "Driver";
                default:               return role;
            }
        }
    }
}