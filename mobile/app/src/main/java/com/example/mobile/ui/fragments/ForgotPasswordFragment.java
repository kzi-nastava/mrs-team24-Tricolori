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
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

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
                Toast.makeText(getContext(), "Reset link sent to: " + email, Toast.LENGTH_SHORT).show();
                // TODO: Retrofit
            }
        });

        // "Login here" (Return back)
        tvBackToLogin.setOnClickListener(v -> {
            Navigation.findNavController(view).popBackStack();
        });
    }
}