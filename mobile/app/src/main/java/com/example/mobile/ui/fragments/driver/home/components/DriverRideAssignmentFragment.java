package com.example.mobile.ui.fragments.driver.home.components;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.mobile.R;
import com.example.mobile.dto.ride.CancellationRequest;
import com.example.mobile.model.RideAssignmentResponse;
import com.example.mobile.network.RetrofitClient;
import com.example.mobile.ui.fragments.driver.home.DriverViewModel;

import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DriverRideAssignmentFragment extends Fragment {

    private static final String TAG = "DriverRideAssignment";

    private DriverViewModel viewModel;
    private Button btnStartRide;
    private Button btnCancelAssignment;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_driver_ride_assignment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Log.d(TAG, "onViewCreated");

        viewModel = new ViewModelProvider(requireActivity()).get(DriverViewModel.class);

        // ============ SETUP BUTTONS ============
        btnStartRide = view.findViewById(R.id.btnStartRide);
        btnCancelAssignment = view.findViewById(R.id.btnCancelAssignment);

        // ============ SLUŠAJ RIDE DETALJE ============
        viewModel.getActiveRide().observe(getViewLifecycleOwner(), ride -> {
            if (ride != null) {
                Log.d(TAG, "Displaying ride: " + ride.id);
                displayRideData(view, ride);
            }
        });

        // ============ SETUP BUTTON LISTENERS ============
        setupListeners();
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

        if (tvName != null) tvName.setText(fullName);
        if (tvPhone != null) tvPhone.setText(ride.passengerPhoneNum);
        if (tvPickup != null) tvPickup.setText(ride.pickupAddress);
        if (tvDest != null) tvDest.setText(ride.destinationAddress);
        if (tvPrice != null && ride.price != null) {
            tvPrice.setText(String.format(Locale.getDefault(), "%.0f RSD", ride.price));
        }
        if (tvDistance != null && ride.distanceKm != null) {
            tvDistance.setText(String.format(Locale.getDefault(), "%.1f km", ride.distanceKm));
        }
        if (tvInitial != null && ride.passengerFirstName != null && !ride.passengerFirstName.isEmpty()) {
            tvInitial.setText(ride.passengerFirstName.substring(0, 1).toUpperCase());
        }
    }

    /**
     * Setup button listeners
     */
    private void setupListeners() {
        // ============ START RIDE BUTTON ============
        btnStartRide.setOnClickListener(v -> {
            Log.d(TAG, "Start ride button clicked");
            startRide();
        });

        // ============ CANCEL/DECLINE BUTTON ============
        btnCancelAssignment.setOnClickListener(v -> {
            Log.d(TAG, "Cancel assignment button clicked");
            viewModel.setRideStatus(DriverViewModel.STATE_CANCEL_RIDE);
        });
    }

    private void startRide() {
        RideAssignmentResponse ride = viewModel.getActiveRide().getValue();

        if (ride == null || ride.id == null) {
            Toast.makeText(getContext(), "No active ride", Toast.LENGTH_SHORT).show();
            return;
        }

        Long rideId = ride.id;
        Log.d(TAG, "Starting ride: " + rideId);

        btnStartRide.setEnabled(false);
        btnStartRide.setAlpha(0.5f);


        // ✅ PRILAGDI ENDPOINT PREMA TVOJEM BACKEND-U
        // Opcije:
        // 1. rideService.completeRide(rideId)  ← PUT /api/v1/rides/{rideId}/complete
        // 2. Trebam novi endpoint poput startRide() ako ne postoji

        // Za sada koristim completeRide() jer je jedina koja postoji
        // ALI PROVJERI SA BACKEND TIMAN KAKO SE STARTUJE VOZNJA!

        RetrofitClient.getRideService(requireContext()).startRide(rideId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Log.d(TAG, "Ride started successfully");
                    Toast.makeText(getContext(), "Ride started!", Toast.LENGTH_SHORT).show();
                    viewModel.setRideStatus(DriverViewModel.STATE_ACTIVE_RIDE);
                } else {
                    Log.e(TAG, "Failed to start ride: " + response.code());
                    Toast.makeText(getContext(), "Failed to start ride", Toast.LENGTH_SHORT).show();
                    btnStartRide.setEnabled(true);
                    btnStartRide.setAlpha(1.0f);
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e(TAG, "Network error", t);
                Toast.makeText(getContext(), "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                btnStartRide.setEnabled(true);
                btnStartRide.setAlpha(1.0f);
            }
        });
    }
}