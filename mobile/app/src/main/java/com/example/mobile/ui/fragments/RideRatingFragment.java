package com.example.mobile.ui.fragments;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.mobile.R;
import com.example.mobile.dto.ride.RideRatingRequest;
import com.example.mobile.network.RetrofitClient;
import com.example.mobile.network.service.RideService;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RideRatingFragment extends Fragment {

    private static final String TAG = "RideRatingFragment";
    private static final String ARG_RIDE_ID     = "ride_id";
    private static final String ARG_DRIVER_NAME = "driver_name";

    // Star drawables – swap these for your own if you have custom assets
    private static final int STAR_FILLED = R.drawable.ic_star_filled;
    private static final int STAR_EMPTY  = R.drawable.ic_star_empty;

    private long rideId;
    private String driverName;

    private int driverRating  = 0;
    private int vehicleRating = 0;

    private ImageView[] driverStars  = new ImageView[5];
    private ImageView[] vehicleStars = new ImageView[5];

    private TextInputEditText etComment;
    private MaterialButton    btnSubmit;
    private MaterialButton    btnSkip;
    private TextView          tvDriverName;


    public static RideRatingFragment newInstance(long rideId, String driverName) {
        RideRatingFragment f = new RideRatingFragment();
        Bundle args = new Bundle();
        args.putLong(ARG_RIDE_ID, rideId);
        args.putString(ARG_DRIVER_NAME, driverName);
        f.setArguments(args);
        return f;
    }

    public RideRatingFragment() {}


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_ride_rating, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rideId     = getArguments().getLong(ARG_RIDE_ID);
        driverName = getArguments().getString(ARG_DRIVER_NAME, "");

        initViews(view);
        setupStars(view);
        setupListeners();
    }


    private void initViews(View view) {
        tvDriverName = view.findViewById(R.id.tvRatingDriverName);
        etComment    = view.findViewById(R.id.etRatingComment);
        btnSubmit    = view.findViewById(R.id.btnSubmitRating);
        btnSkip      = view.findViewById(R.id.btnSkipRating);

        tvDriverName.setText(driverName.isEmpty() ? "Your Driver" : driverName);
    }

    private void setupStars(View view) {
        // Driver stars
        driverStars[0] = view.findViewById(R.id.ivDriverStar1);
        driverStars[1] = view.findViewById(R.id.ivDriverStar2);
        driverStars[2] = view.findViewById(R.id.ivDriverStar3);
        driverStars[3] = view.findViewById(R.id.ivDriverStar4);
        driverStars[4] = view.findViewById(R.id.ivDriverStar5);

        // Vehicle stars
        vehicleStars[0] = view.findViewById(R.id.ivVehicleStar1);
        vehicleStars[1] = view.findViewById(R.id.ivVehicleStar2);
        vehicleStars[2] = view.findViewById(R.id.ivVehicleStar3);
        vehicleStars[3] = view.findViewById(R.id.ivVehicleStar4);
        vehicleStars[4] = view.findViewById(R.id.ivVehicleStar5);

        // Set click listeners
        for (int i = 0; i < 5; i++) {
            final int rating = i + 1;
            driverStars[i].setOnClickListener(v -> setDriverRating(rating));
            vehicleStars[i].setOnClickListener(v -> setVehicleRating(rating));
        }

        // Start with all empty
        renderStars(driverStars, 0);
        renderStars(vehicleStars, 0);
    }

    private void setupListeners() {
        btnSubmit.setOnClickListener(v -> submitRating());
        btnSkip.setOnClickListener(v -> navigateBack());
    }


    private void setDriverRating(int rating) {
        // Tapping the same star again deselects it
        driverRating = (driverRating == rating) ? 0 : rating;
        renderStars(driverStars, driverRating);
        updateSubmitButton();
    }

    private void setVehicleRating(int rating) {
        vehicleRating = (vehicleRating == rating) ? 0 : rating;
        renderStars(vehicleStars, vehicleRating);
        updateSubmitButton();
    }

    private void renderStars(ImageView[] stars, int filledCount) {
        for (int i = 0; i < 5; i++) {
            stars[i].setImageResource(i < filledCount ? STAR_FILLED : STAR_EMPTY);
        }
    }

    /** Submit is only active once both ratings are selected */
    private void updateSubmitButton() {
        boolean bothSelected = driverRating > 0 && vehicleRating > 0;
        btnSubmit.setEnabled(bothSelected);
        btnSubmit.setAlpha(bothSelected ? 1f : 0.5f);
    }

    private void submitRating() {
        if (driverRating == 0 || vehicleRating == 0) {
            Toast.makeText(getContext(),
                    "Please rate both the driver and the vehicle",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        String comment = etComment.getText() != null
                ? etComment.getText().toString().trim() : "";

        RideRatingRequest request = new RideRatingRequest(
                driverRating,
                vehicleRating,
                comment.isEmpty() ? null : comment
        );

        btnSubmit.setEnabled(false);

        RideService rideService =
                RetrofitClient.getClient(requireContext()).create(RideService.class);

        rideService.rateRide(rideId, request)
                .enqueue(new Callback<ResponseBody>() {

                    @Override
                    public void onResponse(Call<ResponseBody> call,
                                           Response<ResponseBody> response) {

                        if (response.isSuccessful()) {
                            Log.d(TAG, "Rating submitted successfully");
                            Toast.makeText(getContext(),
                                    "Thank you for your rating!",
                                    Toast.LENGTH_SHORT).show();
                            navigateBack();
                        } else {
                            Log.e(TAG, "Rating error: " + response.code());
                            Toast.makeText(getContext(),
                                    "Failed to submit rating. Please try again.",
                                    Toast.LENGTH_SHORT).show();
                            btnSubmit.setEnabled(true);
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        Log.e(TAG, "Network failure", t);
                        Toast.makeText(getContext(),
                                "Network error. Please try again.",
                                Toast.LENGTH_SHORT).show();
                        btnSubmit.setEnabled(true);
                    }
                });
    }


    private void navigateBack() {
        if (getParentFragmentManager().getBackStackEntryCount() > 0) {
            getParentFragmentManager().popBackStack();
        }
    }
}