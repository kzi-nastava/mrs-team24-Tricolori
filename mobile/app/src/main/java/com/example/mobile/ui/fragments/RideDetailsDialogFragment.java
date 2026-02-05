package com.example.mobile.ui.fragments;

import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.mobile.R;
import com.example.mobile.dto.ride.DriverRideDetailResponse;
import com.example.mobile.network.RetrofitClient;
import com.example.mobile.network.service.RideService;

import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RideDetailsDialogFragment extends DialogFragment {

    private static final String ARG_RIDE_ID = "ride_id";

    public static RideDetailsDialogFragment newInstance(Long rideId) {
        RideDetailsDialogFragment fragment = new RideDetailsDialogFragment();
        Bundle args = new Bundle();
        args.putLong(ARG_RIDE_ID, rideId);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_ride_details_dialog, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        long rideId = getArguments().getLong(ARG_RIDE_ID);

        fetchRideDetails(rideId, view);

        view.findViewById(R.id.btnClose)
                .setOnClickListener(v -> dismiss());
    }

    private void fetchRideDetails(long rideId, View view) {

        RideService rideService =
                RetrofitClient.getClient(requireContext())
                        .create(RideService.class);

        rideService.getDriverRideDetail(rideId)
                .enqueue(new Callback<DriverRideDetailResponse>() {

                    @Override
                    public void onResponse(
                            Call<DriverRideDetailResponse> call,
                            Response<DriverRideDetailResponse> response) {

                        if (response.isSuccessful() && response.body() != null) {

                            DriverRideDetailResponse dto = response.body();

                            Log.d("RideDTO","createdAt=" + dto.getCreatedAt());
                            Log.d("RideDTO","startedAt=" + dto.getStartedAt());
                            Log.d("RideDTO","completedAt=" + dto.getCompletedAt());

                            bindData(view, dto);

                        } else {
                            Log.e("RideDetails","Error code: " + response.code());
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<DriverRideDetailResponse> call,
                            Throwable t) {
                        Log.e("RideDetails","FAILURE", t);
                    }
                });
    }

    @Override
    public void onStart() {
        super.onStart();

        if (getDialog() != null && getDialog().getWindow() != null) {
            DisplayMetrics dm = new DisplayMetrics();
            getDialog().getWindow().getWindowManager()
                    .getDefaultDisplay().getMetrics(dm);

            getDialog().getWindow().setLayout(
                    (int)(dm.widthPixels * 0.9),
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
        }
    }

    private String formatDateTime(String iso) {
        if (iso == null || iso.length() < 16) return "-";
        return iso.substring(0,10) + " " + iso.substring(11,16);
    }

    private void bindData(View view, DriverRideDetailResponse dto) {

        TextView tvRoute = view.findViewById(R.id.tvDetailRoute);
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
        TextView tvDriverRating = view.findViewById(R.id.tvDriverRating);
        TextView tvVehicleRating = view.findViewById(R.id.tvVehicleRating);
        View ratingContainer = view.findViewById(R.id.ratingContainer);

        tvRoute.setText(String.format(
                "%s → %s",
                safe(dto.getPickupAddress()),
                safe(dto.getDropoffAddress())
        ));
        String start =
                dto.getStartedAt() != null
                        ? dto.getStartedAt()
                        : dto.getCreatedAt();

        tvStartTime.setText(formatDateTime(start));
        tvEndTime.setText(formatDateTime(dto.getCompletedAt()));
        tvDuration.setText(
                dto.getDuration() != null
                        ? dto.getDuration() + " min"
                        : "-"
        );
        tvDistance.setText(
                dto.getDistance() != null
                        ? String.format(Locale.getDefault(),"%.1f km", dto.getDistance())
                        : "-"
        );
        tvPrice.setText(
                dto.getTotalPrice() != null
                        ? String.format(Locale.getDefault(),"%.2f RSD", dto.getTotalPrice())
                        : "-"
        );
        tvStatus.setText(safe(dto.getStatus()));

        tvPassengerName.setText(safe(dto.getPassengerName()));
        tvPassengerPhone.setText(safe(dto.getPassengerPhone()));

        Integer driverRating = dto.getDriverRating();
        Integer vehicleRating = dto.getVehicleRating();

        if (driverRating != null || vehicleRating != null) {

            if (driverRating != null) {
                tvDriverRating.setText(String.format("Driver: %s", stars(driverRating)));
            }
            if (vehicleRating != null) {
                tvVehicleRating.setText(String.format("Vehicle: %s", stars(vehicleRating)));
            }
            ratingContainer.setVisibility(View.VISIBLE);
        } else {
            ratingContainer.setVisibility(View.GONE);
        }
        if (dto.getRatingComment() != null && !dto.getRatingComment().isEmpty()) {
            tvNotes.setText(dto.getRatingComment());
            notesContainer.setVisibility(View.VISIBLE);
        } else {
            notesContainer.setVisibility(View.GONE);
        }
    }

    private String safe(String s) {
        return s == null ? "-" : s;
    }

    private String stars(int rating) {
        StringBuilder s = new StringBuilder();
        for (int i = 0; i < 5; i++) {
            s.append(i < rating ? "★" : "☆");
        }
        return s.toString();
    }

}
