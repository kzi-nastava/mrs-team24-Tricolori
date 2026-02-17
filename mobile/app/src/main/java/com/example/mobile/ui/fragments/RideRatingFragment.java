package com.example.mobile.ui.fragments;

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
import com.example.mobile.dto.ride.PassengerRideDetailResponse;
import com.example.mobile.dto.ride.RideRatingRequest;
import com.example.mobile.network.RetrofitClient;
import com.example.mobile.network.service.RideService;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Locale;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RideRatingFragment extends Fragment {

    private static final String TAG             = "RideRatingFragment";
    private static final String ARG_RIDE_ID     = "ride_id";
    private static final String ARG_DRIVER_NAME = "driver_name";

    private static final int STAR_FILLED = R.drawable.ic_star_filled;
    private static final int STAR_EMPTY  = R.drawable.ic_star_empty;

    private long   rideId;
    private String driverName;

    private int driverRating  = 0;
    private int vehicleRating = 0;

    private final ImageView[] driverStars  = new ImageView[5];
    private final ImageView[] vehicleStars = new ImageView[5];

    private TextInputEditText etComment;
    private MaterialButton    btnSubmit;
    private TextView          tvDriverName;
    private TextView          tvRideRoute;
    private TextView          tvRideDate;
    private TextView          tvRideDriver;
    private TextView          tvRideVehicle;
    private View              vehicleRow;

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
                             ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_ride_rating, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rideId     = getArguments().getLong(ARG_RIDE_ID);
        driverName = getArguments().getString(ARG_DRIVER_NAME, "");

        initViews(view);
        setupStars(view);
        fetchRideDetails();
    }

    private void initViews(View view) {
        tvDriverName  = view.findViewById(R.id.tvRatingDriverName);
        etComment     = view.findViewById(R.id.etRatingComment);
        btnSubmit     = view.findViewById(R.id.btnSubmitRating);
        tvRideRoute   = view.findViewById(R.id.tvRatingRideRoute);
        tvRideDate    = view.findViewById(R.id.tvRatingRideDate);
        tvRideDriver  = view.findViewById(R.id.tvRatingRideDriver);
        tvRideVehicle = view.findViewById(R.id.tvRatingRideVehicle);
        vehicleRow    = view.findViewById(R.id.ratingVehicleRow);

        tvDriverName.setText(driverName.isEmpty() ? "Your Driver" : driverName);
        tvRideDriver.setText(driverName.isEmpty() ? "-" : driverName);

        btnSubmit.setEnabled(true);
        btnSubmit.setAlpha(1f);
        btnSubmit.setOnClickListener(v -> submitRating());

        view.findViewById(R.id.btnSkipRating).setOnClickListener(v -> navigateBack());
    }

    private void fetchRideDetails() {
        RetrofitClient.getClient(requireContext()).create(RideService.class)
                .getPassengerRideDetail(rideId)
                .enqueue(new Callback<PassengerRideDetailResponse>() {
                    @Override
                    public void onResponse(Call<PassengerRideDetailResponse> call,
                                           Response<PassengerRideDetailResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            bindRideInfo(response.body());
                        }
                    }
                    @Override
                    public void onFailure(Call<PassengerRideDetailResponse> call, Throwable t) {
                        Log.w(TAG, "Could not load ride info", t);
                    }
                });
    }

    private void bindRideInfo(PassengerRideDetailResponse dto) {
        String pickup  = dto.getPickupAddress()  != null ? dto.getPickupAddress()  : "-";
        String dropoff = dto.getDropoffAddress() != null ? dto.getDropoffAddress() : "-";
        tvRideRoute.setText(pickup + " → " + dropoff);

        String dateStr = dto.getStartedAt() != null ? dto.getStartedAt() : dto.getCreatedAt();
        if (dateStr != null && dateStr.length() >= 16) {
            tvRideDate.setText(dateStr.substring(0, 10) + "  " + dateStr.substring(11, 16));
        }

        if (dto.getDriverName() != null && !dto.getDriverName().isEmpty()) {
            tvDriverName.setText(dto.getDriverName());
            tvRideDriver.setText(dto.getDriverName());
        }

        if (dto.getVehicleModel() != null && !dto.getVehicleModel().isEmpty()) {
            tvRideVehicle.setText(dto.getVehicleModel());
            vehicleRow.setVisibility(View.VISIBLE);
        } else {
            vehicleRow.setVisibility(View.GONE);
        }

        if (dto.getTotalPrice() != null) {
            View priceRow = getView() != null ? getView().findViewById(R.id.ratingPriceRow) : null;
            if (priceRow != null) {
                ((TextView) getView().findViewById(R.id.tvRatingRidePrice))
                        .setText(String.format(Locale.getDefault(), "%.2f RSD", dto.getTotalPrice()));
                priceRow.setVisibility(View.VISIBLE);
            }
        }
    }

    private void setupStars(View view) {
        driverStars[0] = view.findViewById(R.id.ivDriverStar1);
        driverStars[1] = view.findViewById(R.id.ivDriverStar2);
        driverStars[2] = view.findViewById(R.id.ivDriverStar3);
        driverStars[3] = view.findViewById(R.id.ivDriverStar4);
        driverStars[4] = view.findViewById(R.id.ivDriverStar5);

        vehicleStars[0] = view.findViewById(R.id.ivVehicleStar1);
        vehicleStars[1] = view.findViewById(R.id.ivVehicleStar2);
        vehicleStars[2] = view.findViewById(R.id.ivVehicleStar3);
        vehicleStars[3] = view.findViewById(R.id.ivVehicleStar4);
        vehicleStars[4] = view.findViewById(R.id.ivVehicleStar5);

        for (int i = 0; i < 5; i++) {
            final int rating = i + 1;
            driverStars[i].setOnClickListener(v  -> setDriverRating(rating));
            vehicleStars[i].setOnClickListener(v -> setVehicleRating(rating));
        }

        renderStars(driverStars, 0);
        renderStars(vehicleStars, 0);
    }

    private void setDriverRating(int rating) {
        driverRating = (driverRating == rating) ? 0 : rating;
        renderStars(driverStars, driverRating);
    }

    private void setVehicleRating(int rating) {
        vehicleRating = (vehicleRating == rating) ? 0 : rating;
        renderStars(vehicleStars, vehicleRating);
    }

    private void renderStars(ImageView[] stars, int filled) {
        for (int i = 0; i < 5; i++)
            stars[i].setImageResource(i < filled ? STAR_FILLED : STAR_EMPTY);
    }

    private void submitRating() {
        String comment = etComment.getText() != null
                ? etComment.getText().toString().trim() : "";

        Integer drRating  = driverRating  > 0 ? driverRating  : null;
        Integer vehRating = vehicleRating > 0 ? vehicleRating : null;
        String  cmt       = comment.isEmpty() ? null : comment;

        if (drRating == null && vehRating == null && cmt == null) {
            Toast.makeText(getContext(),
                    "Please provide at least a star rating or a comment",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        btnSubmit.setEnabled(false);

        RetrofitClient.getClient(requireContext()).create(RideService.class)
                .rateRide(rideId, new RideRatingRequest(drRating, vehRating, cmt))
                .enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        if (response.isSuccessful()) {
                            Toast.makeText(getContext(), "Thank you for your rating!", Toast.LENGTH_SHORT).show();
                            navigateBack();
                        } else {
                            Log.e(TAG, "Rating error: " + response.code());
                            Toast.makeText(getContext(), "Failed to submit. Please try again.", Toast.LENGTH_SHORT).show();
                            btnSubmit.setEnabled(true);
                        }
                    }
                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        Log.e(TAG, "Network failure", t);
                        Toast.makeText(getContext(), "Network error. Please try again.", Toast.LENGTH_SHORT).show();
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