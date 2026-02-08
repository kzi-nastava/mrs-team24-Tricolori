package com.example.mobile.ui.fragments;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.mobile.R;
import com.example.mobile.dto.auth.RegisterPassengerRequest;
import com.example.mobile.network.RetrofitClient;
import com.example.mobile.network.service.AuthService;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.Gson;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterFragment extends Fragment {

    private TextInputEditText etFirstName, etLastName, etEmail, etPassword, etRepeatPassword, etAddress, etTelephone;
    private MaterialButton btnSubmit, btnUpload;
    private TextView tvLoginPrompt;
    private ImageView ivProfilePreview;
    private Uri selectedImageUri;

    private final ActivityResultLauncher<String> getContent = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    selectedImageUri = uri;
                    btnUpload.setText("Image Selected ✓");

                    ivProfilePreview.setImageURI(uri);

                    ivProfilePreview.setPadding(0, 0, 0, 0);
                    ivProfilePreview.setImageTintList(null);
                }
            });

    public RegisterFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
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
        etAddress = view.findViewById(R.id.etAddress);
        etTelephone = view.findViewById(R.id.etTelephone);
        btnSubmit = view.findViewById(R.id.btnSubmit);
        btnUpload = view.findViewById(R.id.btnUpload);
        tvLoginPrompt = view.findViewById(R.id.tvLoginPrompt);
        ivProfilePreview = view.findViewById(R.id.ivProfilePreview);
    }

    private void setupListeners(View view) {
        tvLoginPrompt.setOnClickListener(v -> Navigation.findNavController(view).popBackStack());

        btnUpload.setOnClickListener(v -> getContent.launch("image/*"));

        btnSubmit.setOnClickListener(v -> {
            if (validateInput()) {
                sendRegistrationRequest();
            }
        });
    }

    private boolean validateInput() {
        if (etEmail.getText().toString().isEmpty() || etPassword.getText().toString().isEmpty()) {
            Toast.makeText(getContext(), "Email and Password are required!", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (!etPassword.getText().toString().equals(etRepeatPassword.getText().toString())) {
            Toast.makeText(getContext(), "Passwords do not match!", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void sendRegistrationRequest() {
        btnSubmit.setEnabled(false);

        RegisterPassengerRequest regData = new RegisterPassengerRequest(
                etFirstName.getText().toString(),
                etLastName.getText().toString(),
                etEmail.getText().toString(),
                etPassword.getText().toString(),
                etAddress.getText().toString(),
                etTelephone.getText().toString()
        );

        String json = new Gson().toJson(regData);
        RequestBody dataPart = RequestBody.create(json, MediaType.parse("application/json"));

        MultipartBody.Part imagePart = null;
        if (selectedImageUri != null) {
            imagePart = prepareImagePart("image", selectedImageUri);
        }

        AuthService authService = RetrofitClient.getClient(requireContext()).create(AuthService.class);

        authService.registerPassenger(dataPart, imagePart).enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                btnSubmit.setEnabled(true);
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), "Success! Please check your email for activation link.", Toast.LENGTH_LONG).show();
                    Navigation.findNavController(requireView()).popBackStack();
                } else {
                    Toast.makeText(getContext(), "Error " + response.code() + ": Registration failed", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                btnSubmit.setEnabled(true);
                Toast.makeText(getContext(), "Network Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private MultipartBody.Part prepareImagePart(String partName, Uri fileUri) {
        try {
            InputStream inputStream = requireContext().getContentResolver().openInputStream(fileUri);

            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 70, outputStream);

            byte[] bytes = outputStream.toByteArray();
            RequestBody requestFile = RequestBody.create(bytes, MediaType.parse("image/jpeg"));

            return MultipartBody.Part.createFormData(partName, "profile_pic.jpg", requestFile);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}