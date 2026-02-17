package com.example.mobile.ui.fragments.passenger.home.components;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
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
import com.example.mobile.network.service.RideService;
import com.example.mobile.ui.fragments.passenger.home.PassengerViewModel;

import java.util.Locale;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PassengerWaitingFragment extends Fragment {

    private PassengerViewModel viewModel;
    private RideService rideService;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_passenger_waiting, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(PassengerViewModel.class);

        View pulseCircle = view.findViewById(R.id.pulseCircle);
        if (pulseCircle != null) {
            Animation pulse = AnimationUtils.loadAnimation(getContext(), R.anim.pulse);
            pulseCircle.startAnimation(pulse);
        }

        rideService = RetrofitClient.getClient(requireContext()).create(RideService.class);

        viewModel.getActiveRide().observe(getViewLifecycleOwner(), ride -> {
            if (ride != null) {
                displayRideData(view, ride);
            }
        });

        view.findViewById(R.id.btnCancelRidePassenger).setOnClickListener(v -> cancelRide());
    }

    private void displayRideData(View view, RideAssignmentResponse ride) {
        // Driver info
        TextView tvName = view.findViewById(R.id.tvDriverName);
        TextView tvVehicle = view.findViewById(R.id.tvVehicleInfo);
        TextView tvInitial = view.findViewById(R.id.tvDriverInitial);

        // Route info
        TextView tvPickup = view.findViewById(R.id.tvPickupAddr);
        TextView tvDest = view.findViewById(R.id.tvDestinationAddr);
        TextView tvPrice = view.findViewById(R.id.tvEstimatedPrice);
        TextView tvDistance = view.findViewById(R.id.tvDistance);

        tvName.setText(ride.getDriverFullName());
        tvVehicle.setText(ride.getVehicleInfo());
        tvPickup.setText(ride.pickupAddress);
        tvDest.setText(ride.destinationAddress);

        if (ride.price != null) {
            tvPrice.setText(String.format(Locale.getDefault(), "%.0f RSD", ride.price));
        }

        if (ride.distanceKm != null) {
            tvDistance.setText(String.format(Locale.getDefault(), "%.1f km", ride.distanceKm));
        }

        if (ride.driverFirstName != null && !ride.driverFirstName.isEmpty()) {
            tvInitial.setText(ride.driverFirstName.substring(0, 1).toUpperCase());
        }
    }

    private void cancelRide() {
        rideService.cancelRide(new CancellationRequest("")).enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

                if (response.isSuccessful()) {
                    viewModel.clearActiveRide();
                    if (isAdded() && getContext() != null) {
                        Toast.makeText(getContext(), "Ride cancelled", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    if (isAdded() && getContext() != null) {
                        Toast.makeText(getContext(), "Failed to cancel ride: " + response.code(), Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(getContext(), "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}