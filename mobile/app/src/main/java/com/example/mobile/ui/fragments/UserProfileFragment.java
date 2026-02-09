package com.example.mobile.ui.fragments;

import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.mobile.R;
import com.example.mobile.dto.profile.ProfileRequest;
import com.example.mobile.dto.profile.ProfileResponse;
import com.example.mobile.dto.vehicle.VehicleDto;
import com.example.mobile.network.RetrofitClient;
import com.example.mobile.utils.FileUtils;

import java.io.File;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserProfileFragment extends Fragment {
    private EditText etFirstName, etLastName, etPhone, etAddress;
    private TextView tvEmail, tvModel, tvType, tvPlate, tvSeats, tvBabies, tvPets, tvActivity, tvProgressText;
    private ProgressBar progressBar;
    private View vehicleCard, activityCard;
    private Button btnUpdate;

    private Uri selectedFileUri = null;
    private ProfileResponse originalProfile;
    private ProfilePictureFragment pfpFragment;

    public UserProfileFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_user_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        loadProfileData();

        btnUpdate.setOnClickListener(v -> handleProfileUpdate());

        Button btnChangePassword = view.findViewById(R.id.profile_btn_change_password);
        btnChangePassword.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_profile_to_change_password));

        pfpFragment = (ProfilePictureFragment) getChildFragmentManager()
                .findFragmentById(R.id.pfp_fragment_container);

        if (pfpFragment != null) {
            pfpFragment.setOnFileSelectedListener(uri -> {
                this.selectedFileUri = uri;
                checkIfDataChanged();
            });
        }
    }

    private void loadProfileData() {
        RetrofitClient.getProfileService(requireContext())
        .getUserProfile().enqueue(new Callback<ProfileResponse>() {
            @Override
            public void onResponse(@NonNull Call<ProfileResponse> call, @NonNull Response<ProfileResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    populateFields(response.body());
                }
            }
            @Override
            public void onFailure(@NonNull Call<ProfileResponse> call, @NonNull Throwable t) {
                Toast.makeText(getContext(), "Greška: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void handleProfileUpdate() {
        btnUpdate.setEnabled(false);

        if (selectedFileUri != null) {
            uploadImageAndThenUpdate();
        } else {
            sendUpdateRequest(originalProfile.getPfp());
        }
    }

    private void uploadImageAndThenUpdate() {
        File file = FileUtils.getFileFromUri(requireContext(), selectedFileUri);
        if (file == null) return;

        RequestBody requestFile = RequestBody.create(
                MediaType.parse(requireContext().getContentResolver().getType(selectedFileUri)),
                file
        );
        MultipartBody.Part body = MultipartBody.Part.createFormData("pfp", file.getName(), requestFile);

        RetrofitClient.getProfileService(requireContext())
        .uploadPfp(body).enqueue(new Callback<Map<String, String>>() {
            @Override
            public void onResponse(Call<Map<String, String>> call, Response<Map<String, String>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    sendUpdateRequest(response.body().get("url"));
                } else {
                    btnUpdate.setEnabled(true);
                    Toast.makeText(getContext(), "Greška pri uploadu slike", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<Map<String, String>> call, Throwable t) {
                btnUpdate.setEnabled(true);
                Toast.makeText(getContext(), "Mrežna greška pri uploadu", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sendUpdateRequest(String pfpUrl) {
        ProfileRequest request = new ProfileRequest(
            etFirstName.getText().toString().trim(),
            etLastName.getText().toString().trim(),
            etAddress.getText().toString().trim(),
            etPhone.getText().toString().trim(),
            pfpUrl
        );

        if (isDriver()) {
            RetrofitClient.getChangeDataRequestService(requireContext())
            .createRequest(request).enqueue(new Callback<Void>() {
                @Override
                public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                    btnUpdate.setEnabled(true);
                    if (response.isSuccessful()) {
                        Toast.makeText(getContext(), "Request sent to admin!", Toast.LENGTH_LONG).show();
                        resetSelection(); // Kao pfpPicker.reset() na frontu
                        populateFields(originalProfile);
                    } else {
                        Toast.makeText(getContext(), "You already have a pending request!", Toast.LENGTH_SHORT).show();
                    }
                }
                @Override
                public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                    btnUpdate.setEnabled(true);
                }
            });
        } else {
            // Verzija za PUTNIKA (Direktan update)
            RetrofitClient.getProfileService(requireContext())
            .updateProfile(request).enqueue(new Callback<ProfileResponse>() {
                @Override
                public void onResponse(@NonNull Call<ProfileResponse> call, @NonNull Response<ProfileResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        originalProfile = response.body();
                        resetSelection();
                        populateFields(originalProfile);
                        Toast.makeText(getContext(), "Profile updated!", Toast.LENGTH_SHORT).show();
                    }
                }
                @Override
                public void onFailure(@NonNull Call<ProfileResponse> call, @NonNull Throwable t) {
                    btnUpdate.setEnabled(true);
                }
            });
        }
    }

    private void resetSelection() {
        this.selectedFileUri = null;

        ProfilePictureFragment pfpFragment = (ProfilePictureFragment) getChildFragmentManager()
                .findFragmentById(R.id.pfp_fragment_container);

        if (pfpFragment != null) {
            if (originalProfile != null) {
                pfpFragment.setPfpUrl(originalProfile.getPfp());
            } else {
                pfpFragment.setPfpUrl(null);
            }
        }
    }

    private void populateFields(ProfileResponse profile) {
        this.originalProfile = profile;

        etFirstName.setText(profile.getFirstName());
        etLastName.setText(profile.getLastName());
        etPhone.setText(profile.getPhoneNumber());
        etAddress.setText(profile.getHomeAddress());
        tvEmail.setText(profile.getEmail());

        ProfilePictureFragment pfpFragment = (ProfilePictureFragment) getChildFragmentManager()
            .findFragmentById(R.id.pfp_fragment_container);

        if (pfpFragment != null) {
            pfpFragment.setPfpUrl(profile.getPfp());
        }

        VehicleDto vehicle = profile.getVehicle();
        if (vehicle != null && profile.getActiveHours() != null) {
            vehicleCard.setVisibility(View.VISIBLE);
            activityCard.setVisibility(View.VISIBLE);

            tvModel.setText(vehicle.getModel());
            tvPlate.setText(vehicle.getPlateNumber());
            tvType.setText(vehicle.getType());

            String seatStr = vehicle.getNumSeats().toString();
            tvSeats.setText(seatStr);
            tvBabies.setText(vehicle.getBabyFriendly() ? "Yes" : "No");
            tvPets.setText(vehicle.getPetFriendly() ? "Yes" : "No");

            int percentage = (int) Math.round(profile.getActiveHours() / 8 * 100);
            progressBar.setProgress(percentage);

            String percentageStr = percentage + " %";
            tvProgressText.setText(percentageStr);

            String activityStr = String.format("%.1f hours", profile.getActiveHours());
            tvActivity.setText(activityStr);
        } else {
            vehicleCard.setVisibility(View.GONE);
            activityCard.setVisibility(View.GONE);
        }
        btnUpdate.setEnabled(false);
    }

    private void checkIfDataChanged() {
        if (originalProfile == null) return;

        boolean textChanged =
                !etFirstName.getText().toString().equals(originalProfile.getFirstName()) ||
                        !etLastName.getText().toString().equals(originalProfile.getLastName()) ||
                        !etPhone.getText().toString().equals(originalProfile.getPhoneNumber()) ||
                        !etAddress.getText().toString().equals(originalProfile.getHomeAddress());

        boolean imageChanged = (selectedFileUri != null);

        boolean isValid = !etFirstName.getText().toString().trim().isEmpty() &&
                !etLastName.getText().toString().trim().isEmpty();

        btnUpdate.setEnabled((textChanged || imageChanged) && isValid);
    }

    private void initViews(View view) {
        etFirstName = view.findViewById(R.id.profile_first_name);
        etLastName = view.findViewById(R.id.profile_last_name);
        etPhone = view.findViewById(R.id.profile_phone);
        etAddress = view.findViewById(R.id.profile_address);
        tvEmail = view.findViewById(R.id.profile_user_email);

        tvModel = view.findViewById(R.id.profile_vehicle_model);
        tvType = view.findViewById(R.id.profile_vehicle_type);
        tvPlate = view.findViewById(R.id.profile_vehicle_plate);
        tvSeats = view.findViewById(R.id.profile_vehicle_seats);
        tvBabies = view.findViewById(R.id.profile_vehicle_babies);
        tvPets = view.findViewById(R.id.profile_vehicle_pets);

        tvProgressText = view.findViewById(R.id.progressText);
        tvActivity = view.findViewById(R.id.profile_driver_activity);
        progressBar = view.findViewById(R.id.progressBar);
        vehicleCard = view.findViewById(R.id.profile_vehicle_card);
        activityCard = view.findViewById(R.id.profile_activity_card);
        btnUpdate = view.findViewById(R.id.profile_update_profile);

        setChangeTrackers();
    }

    private void setChangeTrackers() {
        TextWatcher watcher = new TextWatcher() {
            @Override public void afterTextChanged(Editable s) {}
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                checkIfDataChanged();
            }
        };
        etFirstName.addTextChangedListener(watcher);
        etLastName.addTextChangedListener(watcher);
        etPhone.addTextChangedListener(watcher);
        etAddress.addTextChangedListener(watcher);
    }

    private boolean isDriver() {
        return originalProfile != null && originalProfile.getVehicle() != null;
    }
}