package com.example.mobile.ui.fragments;

import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import com.example.mobile.dto.ride.DriverRideDetailResponse;
import com.example.mobile.network.RetrofitClient;
import com.example.mobile.network.RideService;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


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

    public static RideDetailsDialogFragment newInstance(Long rideId) {
        RideDetailsDialogFragment fragment = new RideDetailsDialogFragment();
        Bundle args = new Bundle();
        args.putLong(ARG_RIDE_ID, rideId);
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

        long rideId = args.getLong(ARG_RIDE_ID);

        fetchRideDetails(rideId, view);

        Button btnClose = view.findViewById(R.id.btnClose);
        btnClose.setOnClickListener(v -> dismiss());
    }

    private void fetchRideDetails(long rideId, View view) {

        RideService rideService =
                RetrofitClient.getClient().create(RideService.class);

        rideService.getDriverRideDetail(rideId)
                .enqueue(new Callback<DriverRideDetailResponse>() {

                    @Override
                    public void onResponse(
                            Call<DriverRideDetailResponse> call,
                            Response<DriverRideDetailResponse> response) {

                        if (!response.isSuccessful() || response.body() == null) {
                            return;
                        }

                        bindData(view, response.body());
                    }

                    @Override
                    public void onFailure(
                            Call<DriverRideDetailResponse> call,
                            Throwable t) {
                    }
                });
    }



    @Override
    public void onStart() {
        super.onStart();

        if (getDialog() != null && getDialog().getWindow() != null) {
            // Get screen width
            DisplayMetrics displayMetrics = new DisplayMetrics();
            getDialog().getWindow().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            int width = displayMetrics.widthPixels;

            // Set dialog to 90% of screen width (or use fixed dp if preferred)
            int dialogWidth = (int) (width * 0.90);

            getDialog().getWindow().setLayout(
                    dialogWidth,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );

            // Make background transparent so CardView corners show
            getDialog().getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
    }

    private void bindData(View view, DriverRideDetailResponse dto) {

        TextView tvRoute = view.findViewById(R.id.tvDetailRoute);
        TextView tvDate = view.findViewById(R.id.tvDetailDate);
        TextView tvStartTime = view.findViewById(R.id.tvDetailStartTime);
        TextView tvEndTime = view.findViewById(R.id.tvDetailEndTime);
        TextView tvDuration = view.findViewById(R.id.tvDetailDuration);
        TextView tvDistance = view.findViewById(R.id.tvDetailDistance);
        TextView tvPrice = view.findViewById(R.id.tvDetailPrice);
        TextView tvStatus = view.findViewById(R.id.tvDetailStatus);
        TextView tvPassengerName = view.findViewById(R.id.tvDetailPassengerName);
        TextView tvPassengerPhone = view.findViewById(R.id.tvDetailPassengerPhone);
        TextView tvNotes = view.findViewById(R.id.tvDetailNotes);
        View notesContainer = view.findViewById(R.id.notesContainer);

        tvRoute.setText(dto.getPickupAddress() + " → " + dto.getDropoffAddress());

        if (dto.getStartedAt() != null)
            tvDate.setText(dto.getStartedAt().substring(0, 10));

        tvStartTime.setText(dto.getStartedAt());
        tvEndTime.setText(dto.getCompletedAt());

        if (dto.getDuration() != null)
            tvDuration.setText(dto.getDuration() + " min");

        if (dto.getDistance() != null)
            tvDistance.setText(String.format(Locale.getDefault(), "%.1f km", dto.getDistance()));

        if (dto.getTotalPrice() != null)
            tvPrice.setText(String.format(Locale.getDefault(), "€%.2f", dto.getTotalPrice()));

        tvStatus.setText(dto.getStatus());
        tvPassengerName.setText(dto.getPassengerName());
        tvPassengerPhone.setText(dto.getPassengerPhone());

        if (dto.getRatingComment() != null) {
            tvNotes.setText(dto.getRatingComment());
            notesContainer.setVisibility(View.VISIBLE);
        } else {
            notesContainer.setVisibility(View.GONE);
        }
    }

}