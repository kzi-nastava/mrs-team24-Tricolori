package com.example.mobile.ui.fragments;

import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mobile.R;
import com.example.mobile.dto.auth.AdminDriverRegistrationRequest;
import com.example.mobile.network.RetrofitClient;
import com.example.mobile.utils.FileUtils;
import com.google.gson.Gson;

import java.io.File;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminDriverRegistrationFragment extends Fragment {
    private View layoutStepOne, layoutStepTwo;
    private LinearLayout babyFriendlyLayout, petFriendlyLayout;
    private TextView tvStep1Circle, tvStep2Circle, tvStep1Label, tvStep2Label;
    private ProfilePictureFragment pfpFragment;
    private Uri selectedFileUri = null;

    private EditText etFirstName, etLastName, etPhone, etAddress;
    private EditText etEmail, etVehicleModel, etPlate, etSeats;
    private Spinner spinnerVehicleType;

    private Button btnRegister, btnNext, btnPrev;

    public AdminDriverRegistrationFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_admin_driver_registration, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews(view);
        setupValidation();
        validateStepOne();
        validateStepTwo();
        showStep(1);
    }

    private void initViews(View view) {
        // Step indicators:
        layoutStepOne = view.findViewById(R.id.layoutStepOne);
        layoutStepTwo = view.findViewById(R.id.layoutStepTwo);
        tvStep1Circle = view.findViewById(R.id.tvStep1Circle);
        tvStep2Circle = view.findViewById(R.id.tvStep2Circle);
        tvStep1Label = view.findViewById(R.id.tvStep1Label);
        tvStep2Label = view.findViewById(R.id.tvStep2Label);

        // Step one text fields:
        etFirstName = view.findViewById(R.id.etFirstName);
        etLastName = view.findViewById(R.id.etLastName);
        etPhone = view.findViewById(R.id.etPhone);
        etAddress = view.findViewById(R.id.etAddress);

        // Step two text fields:
        etEmail = view.findViewById(R.id.etEmail);
        etVehicleModel = view.findViewById(R.id.etVehicleModel);
        etPlate = view.findViewById(R.id.etPlate);
        etSeats = view.findViewById(R.id.etSeats);

        initPfpFragment();
        initSpinnerVehicleTypes(view);
        initFriendlyButtons(view);
        initButtons(view);
    }

    private void initPfpFragment() {
        pfpFragment = (ProfilePictureFragment) getChildFragmentManager()
                .findFragmentById(R.id.pfp_picker_container);
        if (pfpFragment != null) {
            pfpFragment.setOnFileSelectedListener(uri -> this.selectedFileUri = uri);
        }
    }

    private void initSpinnerVehicleTypes(View view) {
        spinnerVehicleType = view.findViewById(R.id.spinnerVehicleType);
        String[] types = {"STANDARD", "LUXURY", "VAN"};

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(requireContext(),
        android.R.layout.simple_spinner_item, types) {
            @NonNull
            @Override
            public View getView(int position, View convertView, @NonNull ViewGroup parent) {
                View v = super.getView(position, convertView, parent);
                ((TextView) v).setTextColor(R.color.black);
                return v;
            }
        };

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerVehicleType.setAdapter(adapter);
    }

    private void initFriendlyButtons(View view) {
        babyFriendlyLayout = view.findViewById(R.id.babyFriendlyLayout);
        petFriendlyLayout = view.findViewById(R.id.petFriendlyLayout);

        applyFriendlyStyle(babyFriendlyLayout, false);
        applyFriendlyStyle(petFriendlyLayout, false);

        babyFriendlyLayout.setOnClickListener(v -> {
            v.setSelected(!v.isSelected());
            applyFriendlyStyle(babyFriendlyLayout, v.isSelected());
        });

        petFriendlyLayout.setOnClickListener(v -> {
            v.setSelected(!v.isSelected());
            applyFriendlyStyle(petFriendlyLayout, v.isSelected());
        });
    }

    private void applyFriendlyStyle(LinearLayout layout, boolean isSelected) {
        int color = ContextCompat.getColor(requireContext(), isSelected ? R.color.base_600 : R.color.gray_400);
        layout.setBackgroundResource(isSelected ? R.drawable.friendly_card_selected : R.drawable.friendly_card_default);

        ((ImageView) layout.getChildAt(0)).setColorFilter(color);
        ((TextView) layout.getChildAt(1)).setTextColor(color);
    }

    private void initButtons(View view) {
        btnRegister = view.findViewById(R.id.btnRegister);
        btnRegister.setOnClickListener(v -> handleRegistration());

        btnNext = view.findViewById(R.id.btnNext);
        btnNext.setOnClickListener(v -> showStep(2));

        btnPrev = view.findViewById(R.id.btnPrevStep);
        btnPrev.setOnClickListener(v -> showStep(1));
    }

    private void showStep(int step) {
        if (step == 1) {
            layoutStepOne.setVisibility(View.VISIBLE);
            layoutStepTwo.setVisibility(View.GONE);
            updateStepperUI(1);
        } else {
            layoutStepOne.setVisibility(View.GONE);
            layoutStepTwo.setVisibility(View.VISIBLE);
            updateStepperUI(2);
        }
    }
    private void updateStepperUI(int step) {
        int selectedColor = ContextCompat.getColor(requireContext(), R.color.white);
        int unselectedColor = ContextCompat.getColor(requireContext(), R.color.gray_400);
        int labelActiveColor = ContextCompat.getColor(requireContext(), R.color.base_600);

        if (step == 1) {
            tvStep1Circle.setBackgroundResource(R.drawable.bg_stepper_selected);
            tvStep1Circle.setTextColor(selectedColor);
            tvStep1Label.setTextColor(labelActiveColor);

            tvStep2Circle.setBackgroundResource(R.drawable.bg_stepper_unselected);
            tvStep2Circle.setTextColor(unselectedColor);
            tvStep2Label.setTextColor(unselectedColor);
        } else {
            tvStep1Circle.setBackgroundResource(R.drawable.bg_stepper_unselected);
            tvStep1Circle.setTextColor(unselectedColor);
            tvStep1Label.setTextColor(unselectedColor);

            tvStep2Circle.setBackgroundResource(R.drawable.bg_stepper_selected);
            tvStep2Circle.setTextColor(selectedColor);
            tvStep2Label.setTextColor(labelActiveColor);
        }
    }
    private void handleRegistration() {
        AdminDriverRegistrationRequest requestDto = new AdminDriverRegistrationRequest(
            etFirstName.getText().toString().trim(),
            etLastName.getText().toString().trim(),
            etPhone.getText().toString().trim(),
            etAddress.getText().toString().trim(),
            etEmail.getText().toString().trim(),
            etVehicleModel.getText().toString().trim(),
            spinnerVehicleType.getSelectedItem().toString().toUpperCase(),
            etPlate.getText().toString().trim(),
            Integer.parseInt(etSeats.getText().toString().trim()),
            petFriendlyLayout.isSelected(),
            babyFriendlyLayout.isSelected()
        );

        String json = new Gson().toJson(requestDto);

        RequestBody dataPart = RequestBody.create(
                MediaType.parse("application/json"),
                json
        );

        MultipartBody.Part imagePart = null;
        if (selectedFileUri != null) {
            File file = FileUtils.getFileFromUri(requireContext(), selectedFileUri);
            if (file != null) {
                RequestBody requestFile = RequestBody.create(
                        MediaType.parse(requireContext().getContentResolver().getType(selectedFileUri)),
                        file
                );
                imagePart = MultipartBody.Part.createFormData("image", file.getName(), requestFile);
            }
        }

        btnRegister.setEnabled(false);
        RetrofitClient.getAuthService(requireContext())
        .registerDriver(dataPart, imagePart)
        .enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                btnRegister.setEnabled(true);
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), "Success! Check email for final steps.", Toast.LENGTH_LONG).show();
                    resetForm();
                } else {
                    Toast.makeText(getContext(), "Error: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                btnRegister.setEnabled(true);
                Toast.makeText(getContext(), "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void resetForm() {
        EditText[] allFields = {etFirstName, etLastName, etPhone, etAddress, etEmail, etVehicleModel, etPlate, etSeats};
        for (EditText field : allFields) {
            field.setText("");
            field.setError(null);
        }

        int seats = 1;
        String seatsText = etSeats.getText().toString().trim();
        if (!seatsText.isEmpty()) {
            seats = Integer.parseInt(seatsText);
        }

        selectedFileUri = null;
        if (pfpFragment != null) {
            pfpFragment.setPfpUrl(null);
        }

        babyFriendlyLayout.setSelected(false);
        petFriendlyLayout.setSelected(false);
        applyFriendlyStyle(babyFriendlyLayout, false);
        applyFriendlyStyle(petFriendlyLayout, false);

        if (spinnerVehicleType != null && spinnerVehicleType.getAdapter() != null) {
            spinnerVehicleType.setSelection(0);
        }

        showStep(1);
        validateStepOne();
        validateStepTwo();
    }

    private void setupValidation() {
        TextWatcher stepOneWatcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) {
                validateStepOne();
            }
        };

        TextWatcher stepTwoWatcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) {
                validateStepTwo();
            }
        };

        etFirstName.addTextChangedListener(stepOneWatcher);
        etLastName.addTextChangedListener(stepOneWatcher);
        etPhone.addTextChangedListener(stepOneWatcher);
        etAddress.addTextChangedListener(stepOneWatcher);

        etEmail.addTextChangedListener(stepTwoWatcher);
        etVehicleModel.addTextChangedListener(stepTwoWatcher);
        etPlate.addTextChangedListener(stepTwoWatcher);
        etSeats.addTextChangedListener(stepTwoWatcher);
    }
    private void validateStepOne() {
        boolean isValid = !etFirstName.getText().toString().trim().isEmpty() &&
                !etLastName.getText().toString().trim().isEmpty() &&
                !etPhone.getText().toString().trim().isEmpty() &&
                !etAddress.getText().toString().trim().isEmpty();

        btnNext.setEnabled(isValid);
    }
    private void validateStepTwo() {
        String email = etEmail.getText().toString().trim();
        boolean isEmailValid = !email.isEmpty() && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();

        String seatsText = etSeats.getText().toString().trim();
        boolean isSeatsValid = false;

        if (!seatsText.isEmpty()) {
            try {
                int seats = Integer.parseInt(seatsText);
                isSeatsValid = seats >= 1;
            } catch (NumberFormatException ignored) {}
        }

        boolean isVehicleInfoValid = !etVehicleModel.getText().toString().trim().isEmpty() &&
                !etPlate.getText().toString().trim().isEmpty() &&
                isSeatsValid;

        btnRegister.setEnabled(isEmailValid && isVehicleInfoValid);
    }
}
