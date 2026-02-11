package com.example.mobile.ui.fragments;

import android.app.AlertDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.mobile.R;
import com.example.mobile.dto.auth.DriverPasswordSetupRequest;
import com.example.mobile.enums.RegistrationTokenVerificationStatus;
import com.example.mobile.network.RetrofitClient;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PasswordSetupFragment extends Fragment {
    private TextInputEditText newPasswordInput, confirmPasswordInput;
    private MaterialButton btnSubmit;
    private String token;
    private boolean isTokenValid = false;


    public PasswordSetupFragment() {}


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_password_setup, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        newPasswordInput = view.findViewById(R.id.newPasswordSetup);
        confirmPasswordInput = view.findViewById(R.id.confirmPasswordSetup);
        btnSubmit = view.findViewById(R.id.submitPasswordSetup);

        // Disable while we verify token...
        btnSubmit.setEnabled(false);

        if (getArguments() != null) {
            token = getArguments().getString("registrationToken");
        }

        if (token == null || token.isEmpty()) {
            showAlert("Error: Registration token not found.");
        } else {
            verifyTokenOnServer(token);
        }

        btnSubmit.setOnClickListener(v -> onSubmit());
    }

    private void verifyTokenOnServer(String token) {
        RetrofitClient.getAuthService(requireContext())
        .verifyToken(token).enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        RegistrationTokenVerificationStatus status =
                                RegistrationTokenVerificationStatus.valueOf(response.body());

                        handleVerificationStatus(status);
                    } catch (IllegalArgumentException e) {
                        Log.e("ENUM_ERROR", "Unknown status: " + response.body());
                        showAlert("Error while communicating with server.");
                    }
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                Log.e("VERIFICATION", "Error: " + t.getMessage());
                showAlert("Error while verifying token.");
            }
        });
    }

    private void handleVerificationStatus(RegistrationTokenVerificationStatus status) {
        switch (status) {
            case VALID:
                isTokenValid = true;
                btnSubmit.setEnabled(true);
                Toast.makeText(getContext(), "Token verified successfully. Please set your password.", Toast.LENGTH_SHORT).show();
                break;

            case EXPIRED_NEW_SENT:
                showAlert("This link has expired. A new activation link has been sent to your email address.");
                break;

            case ALREADY_ACTIVE:
                showAlert("This account is already active. Please log in to continue.");
                break;

            case INVALID:
                showAlert("The activation token is invalid or does not exist.");
                break;
        }
    }

    private void onSubmit() {
        Log.e("SUBMIT", "Clicked submit of initial password");
        String password = newPasswordInput.getText().toString().trim();
        String confirmPassword = confirmPasswordInput.getText().toString().trim();

        // 1. Check for empty fields
        if (password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(getContext(), "Please fill in all fields.", Toast.LENGTH_SHORT).show();
            return;
        }

        // 2. Check if passwords match
        if (!password.equals(confirmPassword)) {
            Toast.makeText(getContext(), "Passwords do not match!", Toast.LENGTH_SHORT).show();
            return;
        }

        // 3. Password strength validation (Regex)
        // Minimum 8 characters, at least one uppercase letter and one number
        String passwordPattern = "^(?=.*[0-9])(?=.*[A-Z]).{8,}$";
        if (!password.matches(passwordPattern)) {
            Toast.makeText(getContext(),
                    "Password must be at least 8 characters long and contain at least one uppercase letter and one number.",
                    Toast.LENGTH_LONG).show();
            return;
        }

        submitNewPassword(password);
    }

    private void submitNewPassword(String password) {
        DriverPasswordSetupRequest request = new DriverPasswordSetupRequest(this.token, password);

        RetrofitClient.getAuthService(requireContext())
        .driverPasswordSetup(request).enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), "Success! Your account is now active.", Toast.LENGTH_LONG).show();
                    Navigation.findNavController(requireView()).navigate(R.id.action_passwordSetup_to_login);
                } else {
                    String errorMessage = "Activation failed. Please try again.";
                    try {
                        if (response.errorBody() != null) {
                            errorMessage = response.errorBody().string();
                        }
                    } catch (Exception e) {
                        Log.e("API_ERROR", "Error parsing response", e);
                    }
                    Toast.makeText(getContext(), errorMessage, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                Log.e("API_ERROR", "Submit failed: " + t.getMessage());
                Toast.makeText(getContext(), "Submission failed. Please check your connection.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showAlert(String message) {
        new AlertDialog.Builder(requireContext())
            .setTitle("Info")
            .setMessage(message)
            .setCancelable(false)
            .setPositiveButton("OK", (dialog, which) -> {
                if (!isTokenValid) {
                    Navigation.findNavController(requireView()).navigate(R.id.homeFragment);
                }
            })
            .show();
    }
}