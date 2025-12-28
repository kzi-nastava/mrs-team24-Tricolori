package com.example.mobile.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.mobile.R;
import com.example.mobile.ui.models.Ride;

import java.util.Locale;

public class RideDetailsDialogFragment extends DialogFragment {

    private static final String ARG_RIDE_ID = "ride_id";
    private static final String ARG_ROUTE = "route";
    private static final String ARG_START_DATE = "start_date";
    private static final String ARG_END_DATE = "end_date";
    private static final String ARG_PRICE = "price";
    private static final String ARG_STATUS = "status";
    private static final String ARG_START_TIME = "start_time";
    private static final String ARG_END_TIME = "end_time";
    private static final String ARG_DURATION = "duration";
    private static final String ARG_PASSENGER_NAME = "passenger_name";
    private static final String ARG_PASSENGER_PHONE = "passenger_phone";
    private static final String ARG_DISTANCE = "distance";
    private static final String ARG_PAYMENT_METHOD = "payment_method";
    private static final String ARG_NOTES = "notes";

    public static RideDetailsDialogFragment newInstance(Ride ride) {
        RideDetailsDialogFragment fragment = new RideDetailsDialogFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_RIDE_ID, ride.getId());
        args.putString(ARG_ROUTE, ride.getRoute());
        args.putString(ARG_START_DATE, ride.getStartDate());
        args.putString(ARG_END_DATE, ride.getEndDate());
        args.putDouble(ARG_PRICE, ride.getPrice());
        args.putString(ARG_STATUS, ride.getStatus());
        args.putString(ARG_START_TIME, ride.getStartTime());
        args.putString(ARG_END_TIME, ride.getEndTime());
        args.putString(ARG_DURATION, ride.getDuration());
        args.putString(ARG_PASSENGER_NAME, ride.getPassengerName());
        args.putString(ARG_PASSENGER_PHONE, ride.getPassengerPhone());
        args.putDouble(ARG_DISTANCE, ride.getDistance());
        args.putString(ARG_PAYMENT_METHOD, ride.getPaymentMethod());
        args.putString(ARG_NOTES, ride.getNotes());
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_ride_details_dialog, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Bundle args = getArguments();
        if (args == null) return;

        // Bind data to views
        TextView tvRoute = view.findViewById(R.id.tvDetailRoute);
        TextView tvDate = view.findViewById(R.id.tvDetailDate);
        TextView tvStartTime = view.findViewById(R.id.tvDetailStartTime);
        TextView tvEndTime = view.findViewById(R.id.tvDetailEndTime);
        TextView tvDuration = view.findViewById(R.id.tvDetailDuration);
        TextView tvDistance = view.findViewById(R.id.tvDetailDistance);
        TextView tvPrice = view.findViewById(R.id.tvDetailPrice);
        TextView tvPaymentMethod = view.findViewById(R.id.tvDetailPaymentMethod);
        TextView tvStatus = view.findViewById(R.id.tvDetailStatus);
        TextView tvPassengerName = view.findViewById(R.id.tvDetailPassengerName);
        TextView tvPassengerPhone = view.findViewById(R.id.tvDetailPassengerPhone);
        TextView tvNotes = view.findViewById(R.id.tvDetailNotes);
        View notesContainer = view.findViewById(R.id.notesContainer);
        Button btnClose = view.findViewById(R.id.btnClose);

        tvRoute.setText(args.getString(ARG_ROUTE));
        tvDate.setText(args.getString(ARG_START_DATE));
        tvStartTime.setText(args.getString(ARG_START_TIME));
        tvEndTime.setText(args.getString(ARG_END_TIME));
        tvDuration.setText(args.getString(ARG_DURATION));
        tvDistance.setText(String.format(Locale.getDefault(), "%.1f km", args.getDouble(ARG_DISTANCE)));
        tvPrice.setText(String.format(Locale.getDefault(), "€%.2f", args.getDouble(ARG_PRICE)));
        tvPaymentMethod.setText(args.getString(ARG_PAYMENT_METHOD));
        tvStatus.setText(args.getString(ARG_STATUS));
        tvPassengerName.setText(args.getString(ARG_PASSENGER_NAME));
        tvPassengerPhone.setText(args.getString(ARG_PASSENGER_PHONE));

        String notes = args.getString(ARG_NOTES);
        if (notes != null && !notes.isEmpty()) {
            tvNotes.setText(notes);
            notesContainer.setVisibility(View.VISIBLE);
        } else {
            notesContainer.setVisibility(View.GONE);
        }

        btnClose.setOnClickListener(v -> dismiss());
    }
}