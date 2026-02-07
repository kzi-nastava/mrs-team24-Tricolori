package com.example.mobile.ui.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.mobile.R;
import com.example.mobile.dto.pricelist.PriceConfigRequest;
import com.example.mobile.dto.pricelist.PriceConfigResponse;
import com.example.mobile.network.RetrofitClient;
import com.example.mobile.network.service.PricelistService;
import com.google.android.material.textfield.TextInputEditText;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PricelistFragment extends Fragment {

    private static final String TAG = "PricelistFragment";

    private TextInputEditText etPricePerKm;
    private TextInputEditText etStandardPrice;
    private TextInputEditText etLuxuryPrice;
    private TextInputEditText etVanPrice;
    private Button btnSaveChanges;

    private PricelistService pricelistService;
    private boolean isLoading = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_pricelist, container, false);

        initializeViews(view);
        initializeService();
        setupListeners();
        loadCurrentPrices();

        return view;
    }

    private void initializeViews(View view) {
        etPricePerKm = view.findViewById(R.id.etPricePerKm);
        etStandardPrice = view.findViewById(R.id.etStandardPrice);
        etLuxuryPrice = view.findViewById(R.id.etLuxuryPrice);
        etVanPrice = view.findViewById(R.id.etVanPrice);
        btnSaveChanges = view.findViewById(R.id.btnSaveChanges);
    }

    private void initializeService() {
        pricelistService = RetrofitClient.getClient(requireContext())
                .create(PricelistService.class);
    }

    private void setupListeners() {
        btnSaveChanges.setOnClickListener(v -> savePrices());
    }

    private void loadCurrentPrices() {
        if (isLoading) {
            return;
        }

        setLoading(true);

        pricelistService.getCurrentPricing().enqueue(new Callback<PriceConfigResponse>() {
            @Override
            public void onResponse(Call<PriceConfigResponse> call,
                                   Response<PriceConfigResponse> response) {
                setLoading(false);

                Log.d(TAG, "Load prices response code: " + response.code());

                if (!response.isSuccessful() || response.body() == null) {
                    handleLoadError(response);
                    return;
                }

                PriceConfigResponse config = response.body();
                Log.d(TAG, "Prices loaded successfully");

                updateUI(config);
            }

            @Override
            public void onFailure(Call<PriceConfigResponse> call, Throwable t) {
                setLoading(false);

                Log.e(TAG, "Network failure while loading prices: " + t.getMessage());

                Toast.makeText(getContext(),
                        "Network error. Please check your connection.",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateUI(PriceConfigResponse config) {
        if (config.getKmPrice() != null) {
            etPricePerKm.setText(String.valueOf(config.getKmPrice().intValue()));
        }

        if (config.getStandardPrice() != null) {
            etStandardPrice.setText(String.valueOf(config.getStandardPrice().intValue()));
        }

        if (config.getLuxuryPrice() != null) {
            etLuxuryPrice.setText(String.valueOf(config.getLuxuryPrice().intValue()));
        }

        if (config.getVanPrice() != null) {
            etVanPrice.setText(String.valueOf(config.getVanPrice().intValue()));
        }
    }

    private void handleLoadError(Response<PriceConfigResponse> response) {
        String errorBody = "";

        try {
            if (response.errorBody() != null) {
                errorBody = response.errorBody().string();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        Log.e(TAG, "Error loading prices " + response.code() + " : " + errorBody);

        if (response.code() == 401) {
            Toast.makeText(getContext(),
                    "Session expired. Please log in again.",
                    Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getContext(),
                    "Error loading prices. Please try again.",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void savePrices() {
        if (isLoading) {
            return;
        }

        // Validate input
        String pricePerKmStr = etPricePerKm.getText().toString().trim();
        String standardPriceStr = etStandardPrice.getText().toString().trim();
        String luxuryPriceStr = etLuxuryPrice.getText().toString().trim();
        String vanPriceStr = etVanPrice.getText().toString().trim();

        if (pricePerKmStr.isEmpty() || standardPriceStr.isEmpty() ||
                luxuryPriceStr.isEmpty() || vanPriceStr.isEmpty()) {
            Toast.makeText(getContext(),
                    "Please fill in all fields",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            double pricePerKm = Double.parseDouble(pricePerKmStr);
            double standardPrice = Double.parseDouble(standardPriceStr);
            double luxuryPrice = Double.parseDouble(luxuryPriceStr);
            double vanPrice = Double.parseDouble(vanPriceStr);

            // Validate prices are positive
            if (pricePerKm <= 0 || standardPrice <= 0 || luxuryPrice <= 0 || vanPrice <= 0) {
                Toast.makeText(getContext(),
                        "All prices must be greater than zero",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            // Create request object
            PriceConfigRequest request = new PriceConfigRequest(
                    standardPrice,
                    luxuryPrice,
                    vanPrice,
                    pricePerKm
            );

            updatePricesOnServer(request);

        } catch (NumberFormatException e) {
            Toast.makeText(getContext(),
                    "Please enter valid numbers",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void updatePricesOnServer(PriceConfigRequest request) {
        // Check authentication
        SharedPreferences prefs = requireContext()
                .getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);

        String token = prefs.getString("jwt_token", null);

        if (token == null || token.isEmpty()) {
            Toast.makeText(getContext(),
                    "Please log in first",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        setLoading(true);

        pricelistService.updatePricing(request).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                setLoading(false);

                Log.d(TAG, "Update prices response code: " + response.code());

                if (response.isSuccessful()) {
                    Log.d(TAG, "Prices updated successfully");

                    Toast.makeText(getContext(),
                            "Prices updated successfully!",
                            Toast.LENGTH_SHORT).show();

                    // Reload prices to confirm update
                    loadCurrentPrices();
                } else {
                    handleUpdateError(response);
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                setLoading(false);

                Log.e(TAG, "Network failure while updating prices: " + t.getMessage());

                Toast.makeText(getContext(),
                        "Network error. Please check your connection.",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void handleUpdateError(Response<Void> response) {
        String errorBody = "";

        try {
            if (response.errorBody() != null) {
                errorBody = response.errorBody().string();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        Log.e(TAG, "Error updating prices " + response.code() + " : " + errorBody);

        if (response.code() == 401) {
            Toast.makeText(getContext(),
                    "Session expired. Please log in again.",
                    Toast.LENGTH_SHORT).show();
        } else if (response.code() == 403) {
            Toast.makeText(getContext(),
                    "You don't have permission to update prices.",
                    Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getContext(),
                    "Error updating prices. Please try again.",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void setLoading(boolean loading) {
        isLoading = loading;

        if (btnSaveChanges != null) {
            btnSaveChanges.setEnabled(!loading);
            btnSaveChanges.setText(loading ? "Saving..." : "Save changes");
        }

        if (etPricePerKm != null) {
            etPricePerKm.setEnabled(!loading);
            etStandardPrice.setEnabled(!loading);
            etLuxuryPrice.setEnabled(!loading);
            etVanPrice.setEnabled(!loading);
        }
    }
}