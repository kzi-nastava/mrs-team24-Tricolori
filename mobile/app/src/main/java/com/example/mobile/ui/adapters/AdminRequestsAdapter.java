package com.example.mobile.ui.adapters;

import android.content.Context;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mobile.R;
import com.example.mobile.dto.profile.ChangeDataRequestResponse;
import com.example.mobile.dto.profile.ChangeDriverProfileDTO;

import java.util.List;

public class AdminRequestsAdapter extends RecyclerView.Adapter<AdminRequestsAdapter.ViewHolder> {
    // Interface that will help with selecting concrete request:
    public interface OnActionListener {
        void onApprove(long id);
        void onReject(long id);
    }

    private final List<ChangeDataRequestResponse> requests;
    private final OnActionListener listener;

    public AdminRequestsAdapter(List<ChangeDataRequestResponse> requests, OnActionListener listener) {
        this.requests = requests;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_change_request, parent, false);
        return new ViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ChangeDataRequestResponse req = requests.get(position);
        Context context = holder.itemView.getContext();

        holder.tvEmail.setText(req.getEmail());
        holder.tvDriverId.setText(
            context.getString(
                R.string.change_request_driver_id,
                req.getDriverId()
            )
        );

        holder.changesContainer.removeAllViews();

        ChangeDriverProfileDTO oldV = req.getOldValues();
        ChangeDriverProfileDTO newV = req.getNewValues();

        addChangeRow(holder.changesContainer, context.getString(R.string.change_request_first_name), oldV.getFirstName(), newV.getFirstName());
        addChangeRow(holder.changesContainer, context.getString(R.string.change_request_last_name), oldV.getLastName(), newV.getLastName());
        addChangeRow(holder.changesContainer, context.getString(R.string.change_request_phone), oldV.getPhoneNum(), newV.getPhoneNum());
        addChangeRow(holder.changesContainer, context.getString(R.string.change_request_address), oldV.getHomeAddress(), newV.getHomeAddress());

        if (newV.getPfpUrl() != null && !newV.getPfpUrl().equals(oldV.getPfpUrl())) {
            View pfpView = LayoutInflater.from(holder.itemView.getContext())
                    .inflate(R.layout.row_pfp_info, holder.changesContainer, false);
            holder.changesContainer.addView(pfpView);
        }

        holder.btnApprove.setOnClickListener(v -> listener.onApprove(req.getId()));
        holder.btnReject.setOnClickListener(v -> listener.onReject(req.getId()));
    }

    private void addChangeRow(LinearLayout container, String label, String oldVal, String newVal) {
        if (newVal == null || newVal.equals(oldVal)) return;

        View row = LayoutInflater.from(container.getContext()).inflate(R.layout.row_change_detail, container, false);

        TextView tvLabel = row.findViewById(R.id.change_label);
        TextView tvOld = row.findViewById(R.id.old_value);
        TextView tvNew = row.findViewById(R.id.new_value);

        tvLabel.setText(label);

        // Old value strike-through:
        tvOld.setText(oldVal);
        tvOld.setPaintFlags(tvOld.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);

        tvNew.setText(newVal);

        container.addView(row);
    }

    @Override
    public int getItemCount() {
        return requests.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvEmail, tvDriverId;
        LinearLayout changesContainer;
        Button btnApprove, btnReject;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvEmail = itemView.findViewById(R.id.req_email);
            tvDriverId = itemView.findViewById(R.id.req_driver_id);
            changesContainer = itemView.findViewById(R.id.req_changes_container);
            btnApprove = itemView.findViewById(R.id.btn_approve);
            btnReject = itemView.findViewById(R.id.btn_reject);
        }
    }
}