package com.example.mobile.ui.fragments.driver.home.components;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.mobile.R;
import com.example.mobile.dto.ride.CancellationRequest;
import com.example.mobile.network.RetrofitClient;
import com.example.mobile.network.service.RideService;
import com.example.mobile.ui.fragments.driver.home.DriverViewModel;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DriverCancelRideFragment extends Fragment {

    private static final String TAG = "DriverCancelRide";

    private DriverViewModel viewModel;
    private EditText etCancelReason;
    private Button btnConfirmCancel;
    private Button btnGoBack;
    private ImageButton btnClose;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_driver_cancel_ride, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Log.d(TAG, "onViewCreated");

        viewModel = new ViewModelProvider(requireActivity()).get(DriverViewModel.class);

        // ============ SETUP VIEWS ============
        etCancelReason = view.findViewById(R.id.etCancelReason);
        btnConfirmCancel = view.findViewById(R.id.btnConfirmCancel);
        btnGoBack = view.findViewById(R.id.btnGoBack);
        btnClose = view.findViewById(R.id.btnClose);

        // ============ DISABLE CONFIRM BUTTON PO DEFAULTU ============
        btnConfirmCancel.setEnabled(false);
        btnConfirmCancel.setAlpha(0.5f);

        // ============ SETUP LISTENERS ============
        setupListeners();
    }

    private void setupListeners() {
        // ============ TEXT CHANGE LISTENER ============
        etCancelReason.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                boolean isEnabled = !s.toString().trim().isEmpty();
                btnConfirmCancel.setEnabled(isEnabled);
                btnConfirmCancel.setAlpha(isEnabled ? 1.0f : 0.5f);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // ============ CONFIRM CANCEL BUTTON ============
        btnConfirmCancel.setOnClickListener(v -> {
            String reason = etCancelReason.getText().toString().trim();
            Log.d(TAG, "Confirm cancel clicked with reason: " + reason);
            handleCancelRide(reason);
        });

        // ============ GO BACK BUTTON ============
        btnGoBack.setOnClickListener(v -> {
            Log.d(TAG, "Go back clicked");
            viewModel.setRideStatus(DriverViewModel.STATE_ASSIGNED);
        });

        // ============ CLOSE BUTTON ============
        btnClose.setOnClickListener(v -> {
            Log.d(TAG, "Close clicked");
            viewModel.setRideStatus(DriverViewModel.STATE_ASSIGNED);
        });
    }

    /**
     * Otkaži voznju sa razlogom
     *
     * REST API: PUT /api/v1/rides/{rideId}/cancel
     */
    private void handleCancelRide(String reason) {
        Long rideId = viewModel.getActiveRide().getValue().id;

        if (rideId == null) {
            Toast.makeText(getContext(), "No active ride", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d(TAG, "Cancelling ride: " + rideId + " with reason: " + reason);

        btnConfirmCancel.setEnabled(false);
        btnConfirmCancel.setAlpha(0.5f);

        // ✅ Koristi stvarni endpoint: PUT /api/v1/rides/{rideId}/cancel
        RetrofitClient.getRideService(requireContext()).cancelRide(rideId, new CancellationRequest(reason)).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Log.d(TAG, "Ride cancelled successfully");
                    Toast.makeText(getContext(), "Ride canceled successfully!", Toast.LENGTH_SHORT).show();

                    viewModel.clearActiveRide();
                    viewModel.setRideStatus(DriverViewModel.STATE_WAITING);

                } else {
                    Log.e(TAG, "Failed to cancel: " + response.code());
                    handleError(response.code());
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e(TAG, "Network error", t);
                Toast.makeText(getContext(), "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();

                btnConfirmCancel.setEnabled(true);
                btnConfirmCancel.setAlpha(1.0f);
            }
        });
    }

    /*
    private void cancelRide() {
        RideAssignmentResponse ride = viewModel.getActiveRide().getValue();

        if (ride == null || ride.id == null) {
            Toast.makeText(getContext(), "No active ride", Toast.LENGTH_SHORT).show();
            return;
        }

        Long rideId = ride.id;
        Log.d(TAG, "cancelling a ride: " + rideId);

        CancellationRequest cancellationRequest = new CancellationRequest("Driver cancelled the ride");

        btnCancelAssignment.setEnabled(false);

        RetrofitClient.getRideService(requireContext())
                .cancelRide(rideId, cancellationRequest)
                .enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        if (response.isSuccessful()) {
                            Log.d(TAG, "Ride cancelled successfully");
                            Toast.makeText(getContext(), "Ride cancelled successfully", Toast.LENGTH_SHORT).show();
                            viewModel.setRideStatus(DriverViewModel.STATE_WAITING);
                        } else {
                            Log.e(TAG, "Error while cancelling: " + response.code());
                            Toast.makeText(getContext(), "Failed to cancel a ride", Toast.LENGTH_SHORT).show();
                            btnCancelAssignment.setEnabled(true);
                        }
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        Log.e(TAG, "Network error while cancelling a ride", t);
                        Toast.makeText(getContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        btnCancelAssignment.setEnabled(true);
                    }
                });
    }
    */

    /**
     * Rukuj greškami
     */
    private void handleError(int code) {
        String msg = (code == 404) ? "Ride not found" : "Something went wrong (" + code + ")";
        Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();

        btnConfirmCancel.setEnabled(true);
        btnConfirmCancel.setAlpha(1.0f);
    }
}