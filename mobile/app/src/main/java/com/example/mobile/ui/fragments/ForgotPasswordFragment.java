package com.example.mobile.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.mobile.R;
import com.example.mobile.dto.auth.ForgotPasswordRequest;
import com.example.mobile.network.AuthService;
import com.example.mobile.network.RetrofitClient;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;

public class ForgotPasswordFragment extends Fragment {

    private TextInputEditText etEmail;
    private MaterialButton btnSendReset;
    private TextView tvBackToLogin;

    public ForgotPasswordFragment() {
        // Required empty constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_forgot_password, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        etEmail = view.findViewById(R.id.etEmail);
        btnSendReset = view.findViewById(R.id.btnSendReset);
        tvBackToLogin = view.findViewById(R.id.tvBackToLogin);

        // "Send Reset Link"
        btnSendReset.setOnClickListener(v -> {
            String email = etEmail.getText().toString();
            if (email.isEmpty()) {
                Toast.makeText(getContext(), "Please enter your email", Toast.LENGTH_SHORT).show();
            } else {
                sendResetEmail(email);
            }
        });

        // "Login here" (Return back)
        tvBackToLogin.setOnClickListener(v -> {
            Navigation.findNavController(view).popBackStack();
        });
    }

    private void sendResetEmail(String email) {
        btnSendReset.setEnabled(false);
        AuthService authService = RetrofitClient.getClient().create(AuthService.class);
        ForgotPasswordRequest request = new ForgotPasswordRequest(email);

        authService.forgotPassword(request).enqueue(new retrofit2.Callback<ResponseBody>() {

            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                btnSendReset.setEnabled(true);
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), "Email sent!", Toast.LENGTH_SHORT).show();
                    Navigation.findNavController(requireView()).popBackStack();
                } else {
                    Toast.makeText(getContext(), "Error: User not found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                btnSendReset.setEnabled(true);
                Toast.makeText(getContext(), "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}