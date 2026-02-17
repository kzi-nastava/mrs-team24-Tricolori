package com.example.mobile.ui.fragments.driver.home.components;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.mobile.R;
import com.example.mobile.model.RideAssignmentResponse;
import com.example.mobile.ui.fragments.driver.home.DriverViewModel;

import java.util.Locale;

public class DriverRideAssignmentFragment extends Fragment {

    private DriverViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_driver_ride_assignment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(DriverViewModel.class);

        viewModel.getActiveRide().observe(getViewLifecycleOwner(), ride -> {
            if (ride != null) {
                displayRideData(view, ride);
            }
        });

        setupListeners(view);
    }

    private void displayRideData(View view, RideAssignmentResponse ride) {
        TextView tvName = view.findViewById(R.id.tvPassengerName);
        TextView tvPhone = view.findViewById(R.id.tvPassengerPhone);
        TextView tvPickup = view.findViewById(R.id.tvPickupAddr);
        TextView tvDest = view.findViewById(R.id.tvDestinationAddr);
        TextView tvInitial = view.findViewById(R.id.tvPassengerInitial);
        TextView tvPrice = view.findViewById(R.id.tvEstimatedPrice);
        TextView tvDistance = view.findViewById(R.id.tvDistance);

        String fullName = ride.passengerFirstName + " " + ride.passengerLastName;
        tvName.setText(fullName);
        tvPhone.setText(ride.passengerPhoneNum);
        tvPickup.setText(ride.pickupAddress);
        tvDest.setText(ride.destinationAddress);

        if (tvPrice != null && ride.price != null) {
            tvPrice.setText(String.format(Locale.getDefault(), "%.0f RSD", ride.price));
        }

        if (tvDistance != null && ride.distanceKm != null) {
            tvDistance.setText(String.format(Locale.getDefault(), "%.1f km", ride.distanceKm));
        }

        if (ride.passengerFirstName != null && !ride.passengerFirstName.isEmpty()) {
            tvInitial.setText(ride.passengerFirstName.substring(0, 1).toUpperCase());
        }
    }

    private void setupListeners(View view) {
        // Cancel ride
        view.findViewById(R.id.btnCancelAssignment).setOnClickListener(v ->
                viewModel.setRideStatus(DriverViewModel.STATE_CANCEL_RIDE)
        );

        // Start ride
        view.findViewById(R.id.btnStartRide).setOnClickListener(v ->
                viewModel.setRideStatus(DriverViewModel.STATE_ACTIVE_RIDE));
    }
}