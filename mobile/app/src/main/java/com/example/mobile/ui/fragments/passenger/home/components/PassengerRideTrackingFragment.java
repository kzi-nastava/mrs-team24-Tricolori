package com.example.mobile.ui.fragments.passenger.home.components;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;

import com.example.mobile.R;
import com.example.mobile.dto.ride.InconsistencyReportRequest;
import com.example.mobile.dto.ride.PanicRideRequest;
import com.example.mobile.dto.ride.PassengerRideDetailResponse;
import com.example.mobile.dto.ride.RideTrackingResponse;
import com.example.mobile.network.RetrofitClient;
import com.example.mobile.network.service.RideService;
import com.example.mobile.ui.components.MapComponent;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PassengerRideTrackingFragment extends Fragment {
/*
    private static final String TAG = "PassengerRideTracking";
    private static final String ARG_RIDE_ID = "ride_id";
    private static final long TRACKING_INTERVAL_MS = 5000;

    private long rideId;
    private Handler trackingHandler;
    private Runnable trackingRunnable;

    private MapView mapView;
    private MapComponent mapComponent;
    private Marker vehicleMarker;

    private TextView tvEstimatedArrival;
    private TextView tvRemainingDistance;
    private TextView tvPickupAddress;
    private TextView tvDestinationAddress;
    private TextView tvDriverName;
    private TextView tvVehicleModel;
    private TextView tvLicensePlate;
    private TextView tvProgressPercentage;
    private TextView tvTotalDistance;
    private View progressBar;

    private MaterialCardView reportFormCard;
    private EditText etReportDescription;
    private TextView tvReportCharCount;
    private MaterialButton btnShowReportForm;
    private MaterialButton btnSubmitReport;
    private MaterialButton btnCancelReport;
    private MaterialCardView reportSuccessCard;

    private MaterialButton btnPanic;
    private boolean panicTriggered = false;
    private boolean reportFormVisible = false;

    private double totalDistance = 0;
    private double remainingDistance = 0;
    private double lastVehicleLat = 0;
    private double lastVehicleLng = 0;
    private double pickupLat = 0;
    private double pickupLng = 0;
    private double dropoffLat = 0;
    private double dropoffLng = 0;

    public static PassengerRideTrackingFragment newInstance(long rideId) {
        PassengerRideTrackingFragment f = new PassengerRideTrackingFragment();
        Bundle args = new Bundle();
        args.putLong(ARG_RIDE_ID, rideId);
        f.setArguments(args);
        return f;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        Configuration.getInstance().load(requireContext(), PreferenceManager.getDefaultSharedPreferences(requireContext()));
        return inflater.inflate(R.layout.fragment_passenger_ride_tracking, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getArguments() != null) {
            rideId = getArguments().getLong(ARG_RIDE_ID);
        }

        initViews(view);
        setupMap();
        setupListeners();
        loadInitialData();
        startTracking();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mapView != null) mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mapView != null) mapView.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        stopTracking();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        stopTracking();
    }

    private void initViews(View view) {
        mapView = view.findViewById(R.id.mapView);

        tvEstimatedArrival = view.findViewById(R.id.tvEstimatedArrival);
        tvRemainingDistance = view.findViewById(R.id.tvRemainingDistance);
        tvPickupAddress = view.findViewById(R.id.tvPickupAddress);
        tvDestinationAddress = view.findViewById(R.id.tvDestinationAddress);
        tvDriverName = view.findViewById(R.id.tvDriverName);
        tvVehicleModel = view.findViewById(R.id.tvVehicleModel);
        tvLicensePlate = view.findViewById(R.id.tvLicensePlate);
        tvProgressPercentage = view.findViewById(R.id.tvProgressPercentage);
        tvTotalDistance = view.findViewById(R.id.tvTotalDistance);
        progressBar = view.findViewById(R.id.progressBar);

        reportFormCard = view.findViewById(R.id.reportFormCard);
        etReportDescription = view.findViewById(R.id.etReportDescription);
        tvReportCharCount = view.findViewById(R.id.tvReportCharCount);
        btnShowReportForm = view.findViewById(R.id.btnShowReportForm);
        btnSubmitReport = view.findViewById(R.id.btnSubmitReport);
        btnCancelReport = view.findViewById(R.id.btnCancelReport);
        reportSuccessCard = view.findViewById(R.id.reportSuccessCard);

        btnPanic = view.findViewById(R.id.btnPanic);

    }

    private void setupMap() {
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setMultiTouchControls(true);
        mapView.setBuiltInZoomControls(false);
        mapView.getController().setZoom(14.0);

        mapComponent = new MapComponent(mapView, requireContext());
    }

    private void setupListeners() {
        btnShowReportForm.setOnClickListener(v -> toggleReportForm());
        btnCancelReport.setOnClickListener(v -> toggleReportForm());
        btnSubmitReport.setOnClickListener(v -> submitReport());
        btnPanic.setOnClickListener(v -> triggerPanic());

        etReportDescription.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(android.text.Editable s) {
                tvReportCharCount.setText(s.length() + "/500");
                btnSubmitReport.setEnabled(s.length() >= 10 && s.length() <= 500);
            }
        });
    }

    private void loadInitialData() {
        RetrofitClient.getClient(requireContext()).create(RideService.class)
                .getPassengerRideDetail(rideId)
                .enqueue(new Callback<PassengerRideDetailResponse>() {
                    @Override
                    public void onResponse(Call<PassengerRideDetailResponse> call,
                                           Response<PassengerRideDetailResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            updateRideDetails(response.body());
                            drawInitialRoute(response.body());
                        }
                    }
                    @Override
                    public void onFailure(Call<PassengerRideDetailResponse> call, Throwable t) {
                        Log.e(TAG, "Failed to load ride details", t);
                    }
                });
    }

    private void updateRideDetails(PassengerRideDetailResponse detail) {
        tvPickupAddress.setText(detail.getPickupAddress());
        tvDestinationAddress.setText(detail.getDropoffAddress());
        tvDriverName.setText(detail.getDriverName());
        tvVehicleModel.setText(detail.getVehicleModel());
        tvLicensePlate.setText(detail.getVehiclePlate());

        if (detail.getPickupLatitude() != null && detail.getPickupLongitude() != null) {
            pickupLat = detail.getPickupLatitude();
            pickupLng = detail.getPickupLongitude();
        }

        if (detail.getDropoffLatitude() != null && detail.getDropoffLongitude() != null) {
            dropoffLat = detail.getDropoffLatitude();
            dropoffLng = detail.getDropoffLongitude();
        }

        if (detail.getDistance() != null) {
            totalDistance = detail.getDistance();
            remainingDistance = detail.getDistance();
            tvTotalDistance.setText(String.format(Locale.getDefault(), "%.1f km total", totalDistance));
        }

        if (detail.getDuration() != null) {
            tvEstimatedArrival.setText(String.format(Locale.getDefault(), "%d min", detail.getDuration()));
        }
    }

    private void drawInitialRoute(PassengerRideDetailResponse detail) {
        if (pickupLat == 0 || dropoffLat == 0) return;

        GeoPoint start = new GeoPoint(pickupLat, pickupLng);
        GeoPoint end = new GeoPoint(dropoffLat, dropoffLng);

        mapComponent.drawRouteFromPoints(start, end);
    }

    private void startTracking() {
        trackingHandler = new Handler(Looper.getMainLooper());
        trackingRunnable = new Runnable() {
            @Override
            public void run() {
                loadTrackingData();
                if (trackingHandler != null) {
                    trackingHandler.postDelayed(this, TRACKING_INTERVAL_MS);
                }
            }
        };
        trackingHandler.post(trackingRunnable);
    }

    private void stopTracking() {
        if (trackingHandler != null && trackingRunnable != null) {
            trackingHandler.removeCallbacks(trackingRunnable);
            trackingHandler = null;
            trackingRunnable = null;
        }
    }

    private void loadTrackingData() {
        RetrofitClient.getClient(requireContext()).create(RideService.class)
                .trackRide(rideId)
                .enqueue(new Callback<RideTrackingResponse>() {
                    @Override
                    public void onResponse(Call<RideTrackingResponse> call,
                                           Response<RideTrackingResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            updateTrackingData(response.body());
                        }
                    }
                    @Override
                    public void onFailure(Call<RideTrackingResponse> call, Throwable t) {
                        Log.e(TAG, "Tracking update failed", t);
                    }
                });
    }

    private void updateTrackingData(RideTrackingResponse tracking) {

        if (tracking.getCurrentLocation() != null) {
            RideTrackingResponse.VehicleLocationResponse loc = tracking.getCurrentLocation();
            if (loc.getLatitude() != null && loc.getLongitude() != null) {
                lastVehicleLat = loc.getLatitude();
                lastVehicleLng = loc.getLongitude();
                updateVehicleMarker();
            }
            if (loc.getModel() != null) {
                tvVehicleModel.setText(loc.getModel());
            }
            if (loc.getPlateNum() != null) {
                tvLicensePlate.setText(loc.getPlateNum());
            }
        }

        if (tracking.getDriver() != null) {
            String driverName = tracking.getDriver().getFirstName()
                    + " " + tracking.getDriver().getLastName();
            tvDriverName.setText(driverName);
        }

        if (tracking.getRoute() != null && tracking.getRoute().getDistanceKm() != null) {
            remainingDistance = tracking.getRoute().getDistanceKm();
            tvRemainingDistance.setText(
                    String.format(Locale.getDefault(), "%.1f km remaining", remainingDistance));
            updateProgress();
        }

        if (tracking.getEstimatedTimeMinutes() != null) {
            tvEstimatedArrival.setText(
                    String.format(Locale.getDefault(), "%d min", tracking.getEstimatedTimeMinutes()));
        }

        if ("FINISHED".equals(tracking.getStatus())) {
            stopTracking();
            handleRideCompletion();
        }

        if ("PANIC".equals(tracking.getStatus()) && !panicTriggered) {
            panicTriggered = true;
            btnPanic.setEnabled(false);
            btnPanic.setText("PANIC Alert Sent");
            updateVehicleMarker();
        }
    }

    private void updateVehicleMarker() {
        if (lastVehicleLat == 0 && lastVehicleLng == 0) return;

        GeoPoint newPoint = new GeoPoint(lastVehicleLat, lastVehicleLng);

        if (vehicleMarker == null) {
            vehicleMarker = new Marker(mapView);
            vehicleMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER);
            Drawable icon = requireContext().getDrawable(R.drawable.ic_car);
            if (icon != null) {
                icon = icon.mutate();
                int sizeDp = 24;
                float density = getResources().getDisplayMetrics().density;
                int sizePx = (int)(sizeDp * density);

                icon.setBounds(0, 0, sizePx, sizePx);
            }
            vehicleMarker.setIcon(icon);
            mapView.getOverlays().add(vehicleMarker);
        }

        vehicleMarker.setPosition(newPoint);
        mapView.invalidate();
    }

    private void updateProgress() {
        if (totalDistance > 0) {
            double traveled = totalDistance - remainingDistance;
            int percentage = (int) ((traveled / totalDistance) * 100);
            tvProgressPercentage.setText(percentage + "% complete");

            if (getView() != null) {
                ViewGroup.LayoutParams params = progressBar.getLayoutParams();
                params.width = (int) ((traveled / totalDistance) * getView().getWidth() * 0.9);
                progressBar.setLayoutParams(params);
            }
        }
    }

    private void toggleReportForm() {
        reportFormVisible = !reportFormVisible;
        reportFormCard.setVisibility(reportFormVisible ? View.VISIBLE : View.GONE);
        btnShowReportForm.setVisibility(reportFormVisible ? View.GONE : View.VISIBLE);

        if (!reportFormVisible) {
            etReportDescription.setText("");
        }
    }

    private void submitReport() {
        String description = etReportDescription.getText().toString().trim();
        if (description.length() < 10 || description.length() > 500) {
            Toast.makeText(getContext(), "Description must be 10-500 characters", Toast.LENGTH_SHORT).show();
            return;
        }

        btnSubmitReport.setEnabled(false);
        btnSubmitReport.setText("Submitting...");

        RetrofitClient.getClient(requireContext()).create(RideService.class)
                .reportInconsistency(rideId, new InconsistencyReportRequest(description))
                .enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        if (response.isSuccessful()) {
                            reportFormVisible = false;
                            reportFormCard.setVisibility(View.GONE);
                            reportSuccessCard.setVisibility(View.VISIBLE);
                            btnShowReportForm.setVisibility(View.GONE);

                            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                                if (reportSuccessCard != null) {
                                    reportSuccessCard.setVisibility(View.GONE);
                                    btnShowReportForm.setVisibility(View.VISIBLE);
                                }
                            }, 5000);
                        } else {
                            Toast.makeText(getContext(), "Failed to submit report", Toast.LENGTH_SHORT).show();
                            btnSubmitReport.setEnabled(true);
                            btnSubmitReport.setText("Submit Report");
                        }
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        Toast.makeText(getContext(), "Network error", Toast.LENGTH_SHORT).show();
                        btnSubmitReport.setEnabled(true);
                        btnSubmitReport.setText("Submit Report");
                    }
                });
    }

    private void triggerPanic() {
        if (panicTriggered) return;

        PanicRideRequest.VehicleLocation location =
                new PanicRideRequest.VehicleLocation(lastVehicleLat, lastVehicleLng);
        PanicRideRequest request = new PanicRideRequest(location);

        RetrofitClient.getClient(requireContext()).create(RideService.class)
                .panicRide(request)
                .enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        if (response.isSuccessful()) {
                            panicTriggered = true;
                            btnPanic.setEnabled(false);
                            btnPanic.setText("PANIC Alert Sent");
                            stopTracking();
                            updateVehicleMarker();
                            Toast.makeText(getContext(), "Emergency services notified", Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        Toast.makeText(getContext(), "Failed to send panic alert", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void handleRideCompletion() {
        Toast.makeText(getContext(), "Ride completed!", Toast.LENGTH_SHORT).show();
        requireActivity().getSupportFragmentManager().popBackStack();
    }*/
}