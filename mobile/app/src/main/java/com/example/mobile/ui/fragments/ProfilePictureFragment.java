package com.example.mobile.ui.fragments;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;

import android.provider.OpenableColumns;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.mobile.R;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;



public class ProfilePictureFragment extends Fragment {
    public interface OnFileSelectedListener {
        void onFileSelected(Uri fileUri);
    }

    private ImageView profileImageView;
    private TextView tvErrorSize;
    private OnFileSelectedListener listener;

    private final ActivityResultLauncher<String> mGetContent = registerForActivityResult(
        new ActivityResultContracts.GetContent(),
        uri -> {
            if (uri != null) handleImageSelection(uri);
        });

    public ProfilePictureFragment() {
        super(R.layout.fragment_profile_picture);
    }

    public void setOnFileSelectedListener(OnFileSelectedListener listener) {
        this.listener = listener;
    }

    @Override
    public void onViewCreated(@NotNull View view, @Nullable Bundle savedInstanceState) {
        profileImageView = view.findViewById(R.id.pfp_image_view);
        tvErrorSize = view.findViewById(R.id.tv_error_size);
        Button btnUpload = view.findViewById(R.id.btn_upload);

        btnUpload.setOnClickListener(v -> mGetContent.launch("image/*"));
    }

    private void handleImageSelection(Uri uri) {
        long fileSize = getFileSize(uri);
        if (fileSize > 5 * 1024 * 1024) {
            tvErrorSize.setVisibility(View.VISIBLE);
            return;
        }
        tvErrorSize.setVisibility(View.GONE);

        Glide.with(this).load(uri).circleCrop().into(profileImageView);

        if (listener != null) {
            listener.onFileSelected(uri);
        }
    }

    public void setPfpUrl(String url) {
        if (url != null && !url.isEmpty()) {
            Glide.with(this)
                    .load(url)
                    .placeholder(R.drawable.ic_person) // Display picture while loading
                    .error(R.drawable.ic_person)       // Display picture if URL doesn't work
                    .circleCrop()
                    .into(profileImageView);
        } else {
            profileImageView.setImageResource(R.drawable.ic_person);
        }
    }

    private long getFileSize(Uri uri) {
        Cursor cursor = requireContext().getContentResolver().query(uri, null, null, null, null);
        if (cursor == null) return 0;
        int sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE);
        cursor.moveToFirst();
        long size = cursor.getLong(sizeIndex);
        cursor.close();
        return size;
    }
}