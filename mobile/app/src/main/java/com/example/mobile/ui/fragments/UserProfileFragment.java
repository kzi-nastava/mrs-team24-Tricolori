package com.example.mobile.ui.fragments;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.mobile.R;
import com.example.mobile.dto.profile.ProfileRequest;
import com.example.mobile.dto.profile.ProfileResponse;
import com.example.mobile.network.RetrofitClient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserProfileFragment extends Fragment {
    private TextView etEmail;
    private EditText etFirstName;
    private EditText etLastName;
    private EditText etPhone;
    private EditText etAddress;
    private Button btnUpdate;

    // Current profile state:
    private ProfileResponse originalProfile;

    public UserProfileFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_user_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Connect UI components:
        etFirstName = view.findViewById(R.id.profile_first_name);
        etLastName = view.findViewById(R.id.profile_last_name);
        etPhone = view.findViewById(R.id.profile_phone);
        etAddress = view.findViewById(R.id.profile_address);
        etEmail = view.findViewById(R.id.profile_user_email);

        RetrofitClient.getProfileService().getUserProfile().enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<ProfileResponse> call, @NonNull Response<ProfileResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    populateFields(response.body());
                }
            }

            @Override
            public void onFailure(@NonNull Call<ProfileResponse> call, @NonNull Throwable t) {
                Toast.makeText(getContext(), "Greska: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        btnUpdate = view.findViewById(R.id.profile_update_profile);
        setChangeTrackers();
        btnUpdate.setOnClickListener(v -> handleProfileUpdate());

        // Navigate to change password:
        Button btnChangePassword = view.findViewById(R.id.profile_btn_change_password);
        btnChangePassword.setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.action_profile_to_change_password);
        });
    }

    private void populateFields(ProfileResponse profile) {
        this.originalProfile = profile;

        etFirstName.setText(profile.getFirstName());
        etLastName.setText(profile.getLastName());
        etPhone.setText(profile.getPhoneNumber());
        etAddress.setText(profile.getHomeAddress());
        etEmail.setText(profile.getEmail());

        btnUpdate.setEnabled(false);
    }

    private void setChangeTrackers() {
        TextWatcher watcher = new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {}
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                checkIfDataChanged();
            }
        };

        etFirstName.addTextChangedListener(watcher);
        etLastName.addTextChangedListener(watcher);
        etPhone.addTextChangedListener(watcher);
        etAddress.addTextChangedListener(watcher);
    }

    private void checkIfDataChanged() {
        if (originalProfile == null) return;

        boolean changed =
                !etFirstName.getText().toString().equals(originalProfile.getFirstName()) ||
                !etLastName.getText().toString().equals(originalProfile.getLastName()) ||
                !etPhone.getText().toString().equals(originalProfile.getPhoneNumber()) ||
                !etAddress.getText().toString().equals(originalProfile.getHomeAddress());

        boolean isValid =
                !etFirstName.getText().toString().trim().isEmpty() &&
                !etLastName.getText().toString().trim().isEmpty() &&
                !etAddress.getText().toString().trim().isEmpty() &&
                !etPhone.getText().toString().trim().isEmpty();

        btnUpdate.setEnabled(changed && isValid);
    }

    private void handleProfileUpdate() {
        String firstName = etFirstName.getText().toString().trim();
        String lastName = etLastName.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String address = etAddress.getText().toString().trim();

        ProfileRequest request = new ProfileRequest(firstName, lastName, address, phone, originalProfile.getPfp());

        // Disable button while data is being sent:
        btnUpdate.setEnabled(false);

        RetrofitClient.getProfileService().updateProfile(request).enqueue(new Callback<ProfileResponse>() {
            @Override
            public void onResponse(@NonNull Call<ProfileResponse> call, @NonNull Response<ProfileResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // Update current profile with returned profile data:
                    originalProfile = response.body();
                    populateFields(originalProfile);

                    Toast.makeText(getContext(), "Profile successfully updated!", Toast.LENGTH_SHORT).show();
                } else {
                    btnUpdate.setEnabled(true);
                    Toast.makeText(getContext(), "Error while saving!", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(@NonNull Call<ProfileResponse> call, @NonNull Throwable t) {
                btnUpdate.setEnabled(true);
                Toast.makeText(getContext(), "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}