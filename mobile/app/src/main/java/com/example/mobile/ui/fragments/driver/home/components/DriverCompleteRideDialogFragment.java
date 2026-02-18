package com.example.mobile.ui.fragments.driver.home.components;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.navigation.Navigation;

import com.example.mobile.R;
import com.example.mobile.network.RetrofitClient;
import com.example.mobile.network.service.RideService;
import com.google.android.material.button.MaterialButton;

import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DriverCompleteRideDialogFragment extends DialogFragment {

    private static final String TAG = "RideCompleteDialog";

    private static final String ARG_RIDE_ID       = "ride_id";
    private static final String ARG_DISTANCE      = "distance";
    private static final String ARG_DURATION      = "duration";
    private static final String ARG_PRICE         = "price";
    private static final String ARG_PICKUP        = "pickup";
    private static final String ARG_DESTINATION   = "destination";

    public static DriverCompleteRideDialogFragment newInstance(
            long rideId,
            double distance,
            int durationMinutes,
            int priceRsd,
            String pickup,
            String destination
    ) {
        DriverCompleteRideDialogFragment f = new DriverCompleteRideDialogFragment();
        Bundle args = new Bundle();
        args.putLong(ARG_RIDE_ID,     rideId);
        args.putDouble(ARG_DISTANCE,  distance);
        args.putInt(ARG_DURATION,     durationMinutes);
        args.putInt(ARG_PRICE,        priceRsd);
        args.putString(ARG_PICKUP,    pickup);
        args.putString(ARG_DESTINATION, destination);
        f.setArguments(args);
        return f;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Full-width dialog with rounded feel
        setStyle(DialogFragment.STYLE_NO_TITLE, R.style.Theme_MaterialComponents_Light_Dialog_MinWidth);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_complete_ride_dialog, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Bundle args = requireArguments();
        long   rideId      = args.getLong(ARG_RIDE_ID);
        double distance    = args.getDouble(ARG_DISTANCE);
        int    duration    = args.getInt(ARG_DURATION);
        int    price       = args.getInt(ARG_PRICE);
        String pickup      = args.getString(ARG_PICKUP, "");
        String destination = args.getString(ARG_DESTINATION, "");

        // Bind views
        TextView tvDistance    = view.findViewById(R.id.tvDistance);
        TextView tvDuration    = view.findViewById(R.id.tvDuration);
        TextView tvPrice       = view.findViewById(R.id.tvPrice);
        TextView tvPickup      = view.findViewById(R.id.tvPickupAddress);
        TextView tvDestination = view.findViewById(R.id.tvDestinationAddress);
        TextView tvDismiss     = view.findViewById(R.id.tvDismiss);
        MaterialButton btnHome = view.findViewById(R.id.btnBackToHome);

        // Populate
        tvDistance.setText(String.format(Locale.getDefault(), "%.1f km", distance));
        tvDuration.setText(String.format(Locale.getDefault(), "%d min", duration));
        tvPrice.setText(String.format(Locale.getDefault(), "%d RSD", price));
        tvPickup.setText(pickup);
        tvDestination.setText(destination);

        // Non-cancellable — driver must confirm
        setCancelable(false);

        tvDismiss.setOnClickListener(v -> navigateHome());
        btnHome.setOnClickListener(v -> navigateHome());

        // Call backend to mark ride as complete
        completeRideOnBackend(rideId);
    }

    /** Calls PUT /api/v1/rides/{id}/complete */
    private void completeRideOnBackend(long rideId) {
        RetrofitClient.getClient(requireContext())
                .create(RideService.class)
                .completeRide(rideId)
                .enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        if (response.isSuccessful()) {
                            Log.d(TAG, "Ride " + rideId + " marked complete on backend");
                        } else {
                            Log.w(TAG, "completeRide returned " + response.code());
                            showBackendError();
                        }
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        Log.e(TAG, "completeRide network failure", t);
                        showBackendError();
                    }
                });
    }

    private void showBackendError() {
        if (getContext() != null) {
            Toast.makeText(getContext(),
                    "Could not sync completion with server — please check connection",
                    Toast.LENGTH_LONG).show();
        }
    }

    private void navigateHome() {
        dismiss();
        if (getView() != null) {
            Navigation.findNavController(requireActivity(), R.id.nav_host_fragment)
                    .navigate(R.id.driverHomeFragment);
        }
    }
}