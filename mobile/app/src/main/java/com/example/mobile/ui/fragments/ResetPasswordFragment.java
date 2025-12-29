package com.example.mobile.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.mobile.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class ResetPasswordFragment extends Fragment {

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

        etNewPassword = view.findViewById(R.id.etNewPassword);
        etConfirmPassword = view.findViewById(R.id.etConfirmPassword);
        btnSubmit = view.findViewById(R.id.btnSubmit);

        btnSubmit.setOnClickListener(v -> {
            String newPass = etNewPassword.getText().toString();
            String confirmPass = etConfirmPassword.getText().toString();

            if (newPass.isEmpty() || confirmPass.isEmpty()) {
                Toast.makeText(getContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show();
            } else if (!newPass.equals(confirmPass)) {
                Toast.makeText(getContext(), "Passwords do not match", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "Password changed successfully!", Toast.LENGTH_SHORT).show();
                // TODO: Retrofit
            }
        });
    }
}