package com.example.mobile.ui.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mobile.R;
import com.example.mobile.ui.models.Ride;

import java.util.List;
import java.util.Locale;

public class RideHistoryAdapter extends RecyclerView.Adapter<RideHistoryAdapter.RideViewHolder> {
    private List<Ride> rides;
    private OnRideClickListener listener;

    public interface OnRideClickListener {
        void onRideClick(Ride ride);
    }

    public RideHistoryAdapter(List<Ride> rides, OnRideClickListener listener) {
        this.rides = rides;
        this.listener = listener;
    }

    public void updateData(List<Ride> newRides) {
        this.rides = newRides;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RideViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_ride_history, parent, false);
        return new RideViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RideViewHolder holder, int position) {
        holder.bind(rides.get(position));
    }

    @Override
    public int getItemCount() {
        return rides.size();
    }

    class RideViewHolder extends RecyclerView.ViewHolder {
        private TextView tvRoute, tvDate, tvPrice, tvStatus, tvPassenger;
        private CardView cardView;

        public RideViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardView);
            tvRoute = itemView.findViewById(R.id.tvRoute);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvPassenger = itemView.findViewById(R.id.tvPassenger);

            cardView.setOnClickListener(v -> {
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION && listener != null) {
                    listener.onRideClick(rides.get(pos));
                }
            });
        }

        public void bind(Ride ride) {
            tvRoute.setText(ride.getRoute());
            tvDate.setText(ride.getStartDate() + " • " + ride.getStartTime());
            tvPrice.setText(String.format(Locale.getDefault(), "€%.2f", ride.getPrice()));
            tvStatus.setText(ride.getStatus());
            tvPassenger.setText(ride.getPassengerName());

            // Set status color
            if ("Completed".equals(ride.getStatus())) {
                tvStatus.setTextColor(Color.parseColor("#4CAF50"));
            } else if ("Cancelled".equals(ride.getStatus())) {
                tvStatus.setTextColor(Color.parseColor("#F44336"));
            } else {
                tvStatus.setTextColor(Color.parseColor("#FF9800"));
            }
        }
    }
}