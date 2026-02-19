package com.example.mobile.ui.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.mobile.R;
import com.example.mobile.dto.auth.LoginRequest;
import com.example.mobile.dto.auth.LoginResponse;
import com.example.mobile.network.service.AuthService;
import com.example.mobile.network.RetrofitClient;
import com.example.mobile.ui.MainActivity;
import com.google.android.material.textfield.TextInputEditText;

import retrofit2.Call;
import retrofit2.Response;

public class LoginFragment extends Fragment {

    private TextInputEditText etEmail;
    private TextInputEditText etPassword;
    private SharedPreferences sharedPreferences;

    public LoginFragment() {
        // Required empty constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize SharedPreferences
        sharedPreferences = requireActivity().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);

        etEmail = view.findViewById(R.id.etEmail);
        etPassword = view.findViewById(R.id.etPassword);
        Button btnLogin = view.findViewById(R.id.btnLogin);

        btnLogin.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(getContext(), "Fill in all fields!", Toast.LENGTH_SHORT).show();
                return;
            }

            login(email, password);

        });

        // Navigate to registration
        TextView tvRegisterPrompt = view.findViewById(R.id.tvRegisterPrompt);
        tvRegisterPrompt.setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.action_login_to_register);
        });

        // Navigate to forgot password
        TextView tvForgotPassword = view.findViewById(R.id.tvForgotPassword);
        tvForgotPassword.setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.action_login_to_forgotPassword);
        });
    }

    private void login(String email, String password) {

        AuthService authService = RetrofitClient.getClient(requireContext()).create(AuthService.class);
        LoginRequest loginRequest = new LoginRequest(email, password);

        authService.login(loginRequest).enqueue(new retrofit2.Callback<>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    handleLoginSuccess(response.body());
                } else {
                    handleLoginFailure(response);
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                Toast.makeText(getContext(), "Server unreachable: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveUserSession(LoginResponse response) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("is_logged_in", true);
        editor.putString("jwt_token", response.accessToken);
        editor.putString("user_email", response.personDto.email);
        editor.putString("user_role", response.personDto.role.name());
        editor.putLong("user_id", response.personDto.id);
        editor.apply();
    }

    private void navigateBasedOnRole(String role) {
        if (getView() == null) return;

        int destination;
        if ("ROLE_DRIVER".equals(role)) {
            destination = R.id.action_login_to_driverHome;
        } else if ("ROLE_ADMIN".equals(role)) {
            destination = R.id.action_login_to_profile;
        } else if ("ROLE_PASSENGER".equals(role)) {
            destination = R.id.action_login_to_passengerHome;
        } else {
            // fallback
            destination = R.id.action_login_to_home;
        }

        Navigation.findNavController(getView()).navigate(destination);
    }

    private void handleLoginSuccess(LoginResponse response) {
        saveUserSession(response);

        if (getActivity() instanceof MainActivity) {
            MainActivity activity = (MainActivity) getActivity();
            activity.onUserLoggedIn();
        }
        Toast.makeText(getContext(), "Welcome back!", Toast.LENGTH_SHORT).show();
        navigateBasedOnRole(response.personDto.role.name());
    }

    private void handleLoginFailure(Response<LoginResponse> response) {
        if (response.code() == 401) {
            Toast.makeText(getContext(), "Wrong credentials!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getContext(), "Something went wrong: " + response.code(), Toast.LENGTH_SHORT).show();
        }
    }
}