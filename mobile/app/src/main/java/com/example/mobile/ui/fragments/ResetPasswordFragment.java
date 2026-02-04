package com.example.mobile.ui.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.mobile.R;
import com.example.mobile.dto.auth.ResetPasswordRequest;
import com.example.mobile.network.AuthService;
import com.example.mobile.network.RetrofitClient;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;

public class ResetPasswordFragment extends Fragment {

    private TextInputEditText etToken;
    private TextInputEditText etNewPassword;
    private TextInputEditText etConfirmPassword;
    private MaterialButton btnSubmit;

    public ResetPasswordFragment() {
        // Required empty constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_reset_password, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        etToken = view.findViewById(R.id.etToken);
        etNewPassword = view.findViewById(R.id.etNewPassword);
        etConfirmPassword = view.findViewById(R.id.etConfirmPassword);
        btnSubmit = view.findViewById(R.id.btnSubmit);

        handleDeepLink();

        btnSubmit.setOnClickListener(v -> {
            String token = etToken.getText().toString().trim();
            String newPass = etNewPassword.getText().toString().trim();
            String confirmPass = etConfirmPassword.getText().toString().trim();

            if (token.isEmpty() || newPass.isEmpty() || confirmPass.isEmpty()) {
                Toast.makeText(getContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show();
            } else if (!newPass.equals(confirmPass)) {
                Toast.makeText(getContext(), "Passwords do not match", Toast.LENGTH_SHORT).show();
            } else {
                performResetPassword(token, newPass);
            }
        });
    }

    private void handleDeepLink() {
        String tokenFromLink = null;

        if (getArguments() != null && getArguments().containsKey("token")) {
            tokenFromLink = getArguments().getString("token");
        }

        if (tokenFromLink == null && requireActivity().getIntent() != null) {
            Intent intent = requireActivity().getIntent();
            Uri data = intent.getData();
            if (data != null) {
                tokenFromLink = data.getQueryParameter("token");
            }
        }

        if (tokenFromLink != null && !tokenFromLink.isEmpty()) {
            etToken.setText(tokenFromLink);
            etToken.setEnabled(false);
            Toast.makeText(getContext(), "Token applied successfully", Toast.LENGTH_SHORT).show();

            requireActivity().getIntent().setData(null);
        }
    }

    private void performResetPassword(String token, String newPassword) {
        btnSubmit.setEnabled(false);

        AuthService authService = RetrofitClient.getClient().create(AuthService.class);
        ResetPasswordRequest request = new ResetPasswordRequest(token, newPassword);

        authService.resetPassword(request).enqueue(new retrofit2.Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                btnSubmit.setEnabled(true);
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), "Password changed successfully!", Toast.LENGTH_SHORT).show();
                    Navigation.findNavController(requireView()).navigate(R.id.action_resetPassword_to_login);
                } else {
                    Toast.makeText(getContext(), "Error: Invalid or expired token", Toast.LENGTH_SHORT).show();
                    etToken.setEnabled(true);
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                btnSubmit.setEnabled(true);
                Toast.makeText(getContext(), "Connection failed: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}