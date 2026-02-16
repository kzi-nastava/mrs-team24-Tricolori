package com.example.mobile.ui.fragments.driver.home.components;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
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

    private DriverViewModel viewModel;
    private EditText etCancelReason;
    private Button btnConfirmCancel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_driver_cancel_ride, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(DriverViewModel.class);

        etCancelReason = view.findViewById(R.id.etCancelReason);
        btnConfirmCancel = view.findViewById(R.id.btnConfirmCancel);

        btnConfirmCancel.setEnabled(false);
        btnConfirmCancel.setAlpha(0.5f);

        setupListeners(view);
    }

    private void setupListeners(View rootView) {
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

        btnConfirmCancel.setOnClickListener(v -> {
            String reason = etCancelReason.getText().toString().trim();
            handleCancelRide(reason);
        });

        View.OnClickListener goBackAction = v -> viewModel.setRideStatus(DriverViewModel.STATE_ASSIGNED);
        rootView.findViewById(R.id.btnGoBack).setOnClickListener(goBackAction);
        rootView.findViewById(R.id.btnClose).setOnClickListener(goBackAction);
    }

    private void handleCancelRide(String reason) {
        btnConfirmCancel.setEnabled(false);
        btnConfirmCancel.setAlpha(0.5f);

        RideService rideService = RetrofitClient.getClient(requireContext()).create(RideService.class);

        rideService.cancelRide(new CancellationRequest(reason)).enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), "Ride canceled successfully!", Toast.LENGTH_SHORT).show();
                    viewModel.setRideStatus(DriverViewModel.STATE_WAITING);
                } else {
                    handleError(response.code());
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {

                Toast.makeText(getContext(), "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                btnConfirmCancel.setEnabled(true);
                btnConfirmCancel.setAlpha(1.0f);
            }
        });

        viewModel.setRideStatus(DriverViewModel.STATE_WAITING);
    }

    private void handleError(int code) {
        String msg = (code == 404) ? "Ride not found" : "Something went wrong (" + code + ")";
        Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
        btnConfirmCancel.setEnabled(true);
        btnConfirmCancel.setAlpha(1.0f);
    }
}