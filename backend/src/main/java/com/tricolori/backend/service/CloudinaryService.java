package com.tricolori.backend.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.Transformation;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class CloudinaryService {

    private final Cloudinary cloudinary;

    @Value("${cloudinary.default-avatar-url}")
    private String defaultPfpUrl;

    /**
        Upload profile picture to cloudinary and return its url
    */
    public String uploadProfilePicture(MultipartFile pfp, Long personId) {

        try {
            if (pfp.isEmpty()) {
                return defaultPfpUrl;
            }

            validateUpload(pfp);

            Transformation<?> transformation = new Transformation<>()
                    .width(300)
                    .height(300)
                    .crop("fill")
                    .gravity("face");

            var uploadResult = cloudinary.uploader().upload(
                    pfp.getBytes(),
                    ObjectUtils.asMap(
                            "public_id", "user_" + personId,
                            "folder", "profile-pictures",
                            "overwrite", true,
                            "resource_type", "image",
                            "transformation", transformation
                    )
            );

            return (String) uploadResult.get("secure_url");

        } catch (IOException e) {
            throw new RuntimeException("Failed to upload image to Cloudinary: " + e.getMessage(), e);
        }
    }

    private void validateUpload(MultipartFile pfp) {

        if (pfp.getSize() > 5 * 1024 * 1024) { // 5MB
            throw new IllegalArgumentException("File size must be less than 5MB");
        }

        String contentType = pfp.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("Only image files are allowed");
        }
    }

}