package com.example.mobile.ui.fragments.history;

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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PagedRideHistoryAdapter extends RecyclerView.Adapter<PagedRideHistoryAdapter.RideViewHolder> {

    private List<Ride> rides = new ArrayList<>();
    private final OnRideClickListener listener;

    // When true the passenger/person name column is shown (driver & admin views)
    private final boolean showPersonName;

    private final SimpleDateFormat inputDateTimeFormat =
            new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
    private final SimpleDateFormat inputDateFormat =
            new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private final SimpleDateFormat outputFormat =
            new SimpleDateFormat("dd.MM.yyyy • HH:mm", Locale.getDefault());
    private final SimpleDateFormat outputDateOnly =
            new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());

    public interface OnRideClickListener {
        void onRideClick(Ride ride);
    }

    public PagedRideHistoryAdapter(List<Ride> rides, OnRideClickListener listener,
                                   boolean showPersonName) {
        this.rides          = rides != null ? rides : new ArrayList<>();
        this.listener       = listener;
        this.showPersonName = showPersonName;
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
        return rides.size();
    }

    class RideViewHolder extends RecyclerView.ViewHolder {

        private final TextView tvRoute;
        private final TextView tvDate;
        private final TextView tvPrice;
        private final TextView tvStatus;
        private final TextView tvPassenger;
        private final CardView cardView;

        public RideViewHolder(@NonNull View itemView) {
            super(itemView);

            cardView    = itemView.findViewById(R.id.cardView);
            tvRoute     = itemView.findViewById(R.id.tvRoute);
            tvDate      = itemView.findViewById(R.id.tvDate);
            tvPrice     = itemView.findViewById(R.id.tvPrice);
            tvStatus    = itemView.findViewById(R.id.tvStatus);
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
            tvDate.setText(formatDate(ride.getStartDate()));
            tvPrice.setText(String.format(Locale.getDefault(), "%.2f RSD", ride.getPrice()));

            String status = ride.getStatus();
            if (status != null && !status.isEmpty()) {
                tvStatus.setText(formatStatus(status));
                styleStatus(status);
            } else {
                tvStatus.setText("UNKNOWN");
                tvStatus.setTextColor(Color.GRAY);
            }

            // Show passenger name for driver / admin; hide for passenger
            if (showPersonName) {
                String personName = ride.getPassengerName();
                if (personName != null && !personName.isEmpty()) {
                    tvPassenger.setText(personName);
                    tvPassenger.setVisibility(View.VISIBLE);
                } else {
                    tvPassenger.setVisibility(View.GONE);
                }
            } else {
                tvPassenger.setVisibility(View.GONE);
            }
        }

        private String formatDate(String dateStr) {
            if (dateStr == null || dateStr.isEmpty()) return "";
            try {
                Date date;
                if (dateStr.contains("T")) {
                    date = inputDateTimeFormat.parse(dateStr);
                    return outputFormat.format(date);
                } else {
                    date = inputDateFormat.parse(dateStr);
                    return outputDateOnly.format(date);
                }
            } catch (Exception e) {
                e.printStackTrace();
                return dateStr;
            }
        }

        private String formatStatus(String status) {
            if (status == null || status.isEmpty()) return "";
            String formatted = status.replace("_", " ");
            String[] words   = formatted.split(" ");
            StringBuilder result = new StringBuilder();
            for (String word : words) {
                if (word.length() > 0) {
                    result.append(word.substring(0, 1).toUpperCase())
                            .append(word.substring(1).toLowerCase())
                            .append(" ");
                }
            }
            return result.toString().trim();
        }

        private void styleStatus(String status) {
            if (status == null) {
                tvStatus.setTextColor(Color.GRAY);
                return;
            }
            switch (status.toUpperCase()) {
                case "COMPLETED":
                case "FINISHED":
                    tvStatus.setTextColor(Color.parseColor("#15803D"));
                    break;
                case "ONGOING":
                case "IN_PROGRESS":
                case "ACTIVE":
                    tvStatus.setTextColor(Color.parseColor("#1D4ED8"));
                    break;
                case "SCHEDULED":
                case "PENDING":
                case "WAITING":
                    tvStatus.setTextColor(Color.parseColor("#9333EA"));
                    break;
                case "CANCELLED":
                case "CANCELLED_BY_DRIVER":
                case "CANCELLED_BY_PASSENGER":
                case "REJECTED":
                    tvStatus.setTextColor(Color.parseColor("#EA580C"));
                    break;
                case "PANIC":
                case "STOPPED":
                    tvStatus.setTextColor(Color.parseColor("#DC2626"));
                    break;
                default:
                    tvStatus.setTextColor(Color.GRAY);
            }
        }
    }
}