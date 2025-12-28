package com.example.mobile.ui.fragments;

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
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class RegisterFragment extends Fragment {

    private TextInputEditText etFirstName, etLastName, etEmail, etPassword, etRepeatPassword;
    private MaterialButton btnSubmit, btnUpload;
    private TextView tvLoginPrompt;

    public RegisterFragment() {
        // Required empty constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_register, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        setupListeners(view);
    }

    private void initViews(View view) {
        etFirstName = view.findViewById(R.id.etFirstName);
        etLastName = view.findViewById(R.id.etLastName);
        etEmail = view.findViewById(R.id.etEmail);
        etPassword = view.findViewById(R.id.etPassword);
        etRepeatPassword = view.findViewById(R.id.etRepeatPassword);
        btnSubmit = view.findViewById(R.id.btnSubmit);
        btnUpload = view.findViewById(R.id.btnUpload);
        tvLoginPrompt = view.findViewById(R.id.tvLoginPrompt);
    }

    private void setupListeners(View view) {
        // "Already have an account? Log back in"
        tvLoginPrompt.setOnClickListener(v -> {
            Navigation.findNavController(view).popBackStack();
        });

        btnSubmit.setOnClickListener(v -> {
            String pass = etPassword.getText().toString();
            String repeatPass = etRepeatPassword.getText().toString();

            if (!pass.equals(repeatPass)) {
                Toast.makeText(getContext(), "Passwords do not match!", Toast.LENGTH_SHORT).show();
            } else {
                // TODO: retrofit
                Toast.makeText(getContext(), "Registration!", Toast.LENGTH_SHORT).show();
            }
        });

        btnUpload.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Open Gallery...", Toast.LENGTH_SHORT).show();
        });
    }
}