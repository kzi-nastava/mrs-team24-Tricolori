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

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class RideHistoryAdapter extends RecyclerView.Adapter<RideHistoryAdapter.RideViewHolder> {

    private List<Ride> rides = new ArrayList<>();
    private OnRideClickListener listener;

    public interface OnRideClickListener {
        void onRideClick(Ride ride);
    }

    public RideHistoryAdapter(List<Ride> rides, OnRideClickListener listener) {
        this.rides = rides != null ? rides : new ArrayList<>();
        this.listener = listener;
    }

    public void updateData(List<Ride> newRides) {
        this.rides = newRides != null ? newRides : new ArrayList<>();
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
        return rides == null ? 0 : rides.size();
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
            if (ride == null) return;

            tvRoute.setText(ride.getRoute());
            tvDate.setText(ride.getStartDate());
            tvPrice.setText(String.format(Locale.getDefault(), "%.2f RSD", ride.getPrice()));
            tvStatus.setText(ride.getStatus());
            tvPassenger.setText(ride.getPassengerName());

            String status = ride.getStatus();

            if (status == null) {
                tvStatus.setTextColor(Color.GRAY);
                return;
            }

            switch (status) {

                case "FINISHED":
                    tvStatus.setTextColor(Color.parseColor("#4CAF50")); 
                    break;

                case "ONGOING":
                    tvStatus.setTextColor(Color.parseColor("#2196F3"));
                    break;

                case "SCHEDULED":
                    tvStatus.setTextColor(Color.parseColor("#9C27B0"));
                    break;

                case "CANCELLED_BY_DRIVER":
                case "CANCELLED_BY_PASSENGER":
                    tvStatus.setText("CANCELLED");
                    tvStatus.setTextColor(Color.parseColor("#FF9800"));
                    break;

                case "REJECTED":
                    tvStatus.setTextColor(Color.parseColor("#FF9800"));
                    break;

                case "STOPPED":
                    tvStatus.setTextColor(Color.parseColor("#FF9800"));
                    break;

                case "PANIC":
                    tvStatus.setTextColor(Color.parseColor("#F44336"));
                    break;

                default:
                    tvStatus.setTextColor(Color.GRAY);
            }
        }
    }
}