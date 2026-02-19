package com.example.mobile.ui.fragments.driver.home.components;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;

import com.example.mobile.R;
import com.example.mobile.dto.pricelist.PriceConfigResponse;
import com.example.mobile.dto.ride.DriverRideDetailResponse;
import com.example.mobile.dto.ride.PanicRideRequest;
import com.example.mobile.dto.ride.RideTrackingResponse;
import com.example.mobile.dto.ride.StopRideRequest;
import com.example.mobile.dto.ride.StopRideResponse;
import com.example.mobile.dto.vehicle.UpdateVehicleLocationRequest;
import com.example.mobile.network.RetrofitClient;
import com.example.mobile.network.service.PricelistService;
import com.example.mobile.network.service.RideService;
import com.example.mobile.ui.components.MapComponent;
import com.google.android.material.button.MaterialButton;

import org.osmdroid.bonuspack.routing.OSRMRoadManager;
import org.osmdroid.bonuspack.routing.Road;
import org.osmdroid.bonuspack.routing.RoadManager;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DriverRideTrackingFragment extends Fragment {
/*
    private static final String TAG = "DriverRideTracking";
    private static final String ARG_RIDE_ID = "ride_id";
    private static final long SIMULATION_INTERVAL_MS = 3_000L;
    private static final int POINTS_PER_TICK = 15;
    private static final double FALLBACK_BASE_PRICE = 150.0;
    private static final double FALLBACK_KM_PRICE = 80.0;

    private long rideId;
    private double totalDistance = 0;
    private double remainingDistance = 0;
    private int totalDurationMin = 0;
    private double pickupLat, pickupLng, dropoffLat, dropoffLng;
    private String pickupAddress = "";
    private String dropoffAddress = "";
    private String passengerName = "";
    private Double cachedBasePrice = null;
    private Double cachedKmPrice = null;
    private boolean panicTriggered = false;
    private boolean stopTriggered = false;

    private List<GeoPoint> routePoints = new ArrayList<>();
    private int currentIndex = 0;
    private Handler simHandler;
    private Runnable simRunnable;

    private MapView mapView;
    private MapComponent mapComponent;
    private Marker vehicleMarker;

    private TextView tvEstimatedArrival;
    private TextView tvRemainingDistance;
    private TextView tvPickupAddress;
    private TextView tvDestinationAddress;
    private TextView tvPassengerName;
    private TextView tvProgressPercentage;
    private TextView tvTotalDistance;
    private View progressBar;
    private MaterialButton btnStopRide;
    private MaterialButton btnPanic;

    public static DriverRideTrackingFragment newInstance(long rideId) {
        DriverRideTrackingFragment f = new DriverRideTrackingFragment();
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
        Configuration.getInstance().load(
                requireContext(),
                PreferenceManager.getDefaultSharedPreferences(requireContext())
        );
        return inflater.inflate(R.layout.fragment_driver_ride_tracking, container, false);
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
        fetchPricelist();
        loadRideData();
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
    public void onDestroyView() {
        super.onDestroyView();
        stopSimulation();
    }

    private void initViews(View view) {
        mapView              = view.findViewById(R.id.mapView);
        tvEstimatedArrival   = view.findViewById(R.id.tvEstimatedArrival);
        tvRemainingDistance  = view.findViewById(R.id.tvRemainingDistance);
        tvPickupAddress      = view.findViewById(R.id.tvPickupAddress);
        tvDestinationAddress = view.findViewById(R.id.tvDestinationAddress);
        tvPassengerName      = view.findViewById(R.id.tvPassengerName);
        tvProgressPercentage = view.findViewById(R.id.tvProgressPercentage);
        tvTotalDistance      = view.findViewById(R.id.tvTotalDistance);
        progressBar          = view.findViewById(R.id.progressBar);
        btnStopRide          = view.findViewById(R.id.btnStopRide);
        btnPanic             = view.findViewById(R.id.btnPanic);
    }

    private void setupMap() {
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setMultiTouchControls(true);
        mapView.setBuiltInZoomControls(false);
        mapView.getController().setZoom(14.0);
        mapComponent = new MapComponent(mapView, requireContext());
    }

    private void setupListeners() {
        btnPanic.setOnClickListener(v -> triggerPanic());
        btnStopRide.setOnClickListener(v -> triggerStop());
    }

    private void fetchPricelist() {
        RetrofitClient.getClient(requireContext())
                .create(PricelistService.class)
                .getCurrentPricing()
                .enqueue(new Callback<PriceConfigResponse>() {
                    @Override
                    public void onResponse(Call<PriceConfigResponse> call,
                                           Response<PriceConfigResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            PriceConfigResponse p = response.body();
                            cachedBasePrice = p.getStandardPrice() != null ? p.getStandardPrice() : FALLBACK_BASE_PRICE;
                            cachedKmPrice = p.getKmPrice() != null ? p.getKmPrice() : FALLBACK_KM_PRICE;
                        } else {
                            Log.w(TAG, "Pricelist " + response.code() + " — using fallback");
                        }
                    }

                    @Override
                    public void onFailure(Call<PriceConfigResponse> call, Throwable t) {
                        Log.e(TAG, "fetchPricelist failed", t);
                    }
                });
    }

    private int calculatePrice() {
        double base  = cachedBasePrice != null ? cachedBasePrice : FALLBACK_BASE_PRICE;
        double perKm = cachedKmPrice   != null ? cachedKmPrice   : FALLBACK_KM_PRICE;
        return (int) Math.round(base + totalDistance * perKm);
    }

    private void loadRideData() {
        RetrofitClient.getClient(requireContext())
                .create(RideService.class)
                .trackRide(rideId)
                .enqueue(new Callback<RideTrackingResponse>() {
                    @Override
                    public void onResponse(Call<RideTrackingResponse> call,
                                           Response<RideTrackingResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            applyTrackingResponse(response.body());
                        }
                        loadDetailData();
                    }

                    @Override
                    public void onFailure(Call<RideTrackingResponse> call, Throwable t) {
                        Log.e(TAG, "trackRide failed", t);
                        loadDetailData();
                    }
                });
    }

    private void applyTrackingResponse(RideTrackingResponse tracking) {
        if (tracking.getRoute().getDistanceKm() != null) {
            totalDistance     = tracking.getRoute().getDistanceKm();
            remainingDistance = totalDistance;
        }
        if (tracking.getEstimatedTimeMinutes() != null) {
            totalDurationMin = tracking.getEstimatedTimeMinutes();
        }
    }

    private void loadDetailData() {
        RetrofitClient.getClient(requireContext())
                .create(RideService.class)
                .getDriverRideDetail(rideId)
                .enqueue(new Callback<DriverRideDetailResponse>() {
                    @Override
                    public void onResponse(Call<DriverRideDetailResponse> call,
                                           Response<DriverRideDetailResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            applyDetailResponse(response.body());
                        } else {
                            Log.w(TAG, "getDriverRideDetail returned " + response.code());
                        }
                    }

                    @Override
                    public void onFailure(Call<DriverRideDetailResponse> call, Throwable t) {
                        Log.e(TAG, "getDriverRideDetail failed", t);
                    }
                });
    }

    private void applyDetailResponse(DriverRideDetailResponse detail) {
        pickupLat      = detail.getPickupLatitude()   != null ? detail.getPickupLatitude()   : 0;
        pickupLng      = detail.getPickupLongitude()  != null ? detail.getPickupLongitude()  : 0;
        dropoffLat     = detail.getDropoffLatitude()  != null ? detail.getDropoffLatitude()  : 0;
        dropoffLng     = detail.getDropoffLongitude() != null ? detail.getDropoffLongitude() : 0;
        pickupAddress  = detail.getPickupAddress()    != null ? detail.getPickupAddress()    : "";
        dropoffAddress = detail.getDropoffAddress()   != null ? detail.getDropoffAddress()   : "";
        passengerName  = detail.getPassengerName()    != null ? detail.getPassengerName()    : "";

        if (totalDistance == 0 && detail.getDistance() != null) {
            totalDistance     = detail.getDistance();
            remainingDistance = totalDistance;
        }
        if (totalDurationMin == 0 && detail.getDuration() != null) {
            totalDurationMin = detail.getDuration();
        }

        new Handler(Looper.getMainLooper()).post(() -> {
            tvPickupAddress.setText(pickupAddress);
            tvDestinationAddress.setText(dropoffAddress);
            tvPassengerName.setText(passengerName);
            tvTotalDistance.setText(String.format(Locale.getDefault(), "%.1f km total", totalDistance));
            tvEstimatedArrival.setText(String.format(Locale.getDefault(), "%d min", totalDurationMin));
            tvRemainingDistance.setText(String.format(Locale.getDefault(), "%.1f km remaining", remainingDistance));
            drawRouteAndBegin();
        });
    }

    private void drawRouteAndBegin() {
        if (pickupLat == 0 || dropoffLat == 0) return;
        GeoPoint start = new GeoPoint(pickupLat, pickupLng);
        GeoPoint end   = new GeoPoint(dropoffLat, dropoffLng);

        new Thread(() -> {
            try {
                OSRMRoadManager roadManager = new OSRMRoadManager(requireContext(), "ANDROID");
                ArrayList<GeoPoint> waypoints = new ArrayList<>();
                waypoints.add(start);
                waypoints.add(end);
                Road road = roadManager.getRoad(waypoints);
                Polyline roadOverlay = RoadManager.buildRoadOverlay(road);
                List<GeoPoint> points = roadOverlay.getActualPoints();

                mapView.post(() -> {
                    mapComponent.drawRoute(points);

                    routePoints  = new ArrayList<>(points);
                    currentIndex = 0;

                    if (!routePoints.isEmpty()) {
                        placeOrMoveVehicleMarker(routePoints.get(0));
                    }

                    startSimulation();
                });
            } catch (Exception e) {
                Log.e(TAG, "drawRouteAndBegin failed", e);
            }
        }).start();
    }

    private void placeOrMoveVehicleMarker(GeoPoint point) {
        if (vehicleMarker == null) {
            vehicleMarker = new Marker(mapView);
            vehicleMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER);
            Drawable icon = requireContext().getDrawable(R.drawable.ic_car);
            if (icon != null) {
                icon = icon.mutate();
                int sizePx = (int) (24 * getResources().getDisplayMetrics().density);
                icon.setBounds(0, 0, sizePx, sizePx);
                vehicleMarker.setIcon(icon);
            }
            mapView.getOverlays().add(vehicleMarker);
        }
        vehicleMarker.setPosition(point);
        mapView.invalidate();
    }

    private void startSimulation() {
        simHandler  = new Handler(Looper.getMainLooper());
        simRunnable = new Runnable() {
            @Override
            public void run() {
                advanceVehicle();
                if (simHandler != null && currentIndex < routePoints.size() - 1) {
                    simHandler.postDelayed(this, SIMULATION_INTERVAL_MS);
                }
            }
        };
        simHandler.postDelayed(simRunnable, SIMULATION_INTERVAL_MS);
    }

    private void stopSimulation() {
        if (simHandler != null && simRunnable != null) {
            simHandler.removeCallbacks(simRunnable);
        }
        simHandler  = null;
        simRunnable = null;
    }

    private void advanceVehicle() {
        if (routePoints.isEmpty()) return;

        currentIndex = Math.min(currentIndex + POINTS_PER_TICK, routePoints.size() - 1);
        GeoPoint next = routePoints.get(currentIndex);
        placeOrMoveVehicleMarker(next);
        updateBackendVehicleLocation(next.getLatitude(), next.getLongitude());

        double progress   = (double) currentIndex / (routePoints.size() - 1);
        remainingDistance = totalDistance * (1 - progress);
        int remainingMin  = (int) Math.ceil(totalDurationMin * (1 - progress));

        tvRemainingDistance.setText(String.format(Locale.getDefault(), "%.1f km remaining", remainingDistance));
        tvEstimatedArrival.setText(String.format(Locale.getDefault(), "%d min", Math.max(0, remainingMin)));
        updateProgressBar(progress);

        if (currentIndex >= routePoints.size() - 1) {
            stopSimulation();
            remainingDistance = 0;
            tvRemainingDistance.setText("0.0 km remaining");
            tvEstimatedArrival.setText("0 min");
            updateProgressBar(1.0);
            showCompletionDialog();
        }
    }

    private void updateProgressBar(double progress) {
        tvProgressPercentage.setText((int)(progress * 100) + "% complete");
        if (getView() != null && getView().getWidth() > 0) {
            ViewGroup.LayoutParams params = progressBar.getLayoutParams();
            int barMaxWidth = getView().getWidth() - (int)(48 * getResources().getDisplayMetrics().density);
            params.width = (int)(barMaxWidth * progress);
            progressBar.setLayoutParams(params);
        }
    }

    private void updateBackendVehicleLocation(double latitude, double longitude) {
        RetrofitClient.getClient(requireContext())
                .create(RideService.class)
                .updateRideVehicleLocation(rideId, new UpdateVehicleLocationRequest(latitude, longitude))
                .enqueue(new Callback<Void>() {
                    @Override public void onResponse(Call<Void> c, Response<Void> r) {}
                    @Override public void onFailure(Call<Void> c, Throwable t) {
                        Log.e(TAG, "updateVehicleLocation failed", t);
                    }
                });
    }

    private void triggerPanic() {
        if (panicTriggered) return;
        GeoPoint pos = vehicleMarker != null ? vehicleMarker.getPosition() : new GeoPoint(pickupLat, pickupLng);
        RetrofitClient.getClient(requireContext())
                .create(RideService.class)
                .panicRide(new PanicRideRequest(
                        new PanicRideRequest.VehicleLocation(pos.getLatitude(), pos.getLongitude())))
                .enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        if (response.isSuccessful()) {
                            panicTriggered = true;
                            stopSimulation();
                            btnPanic.setEnabled(false);
                            btnPanic.setText("PANIC Alert Sent");
                            Toast.makeText(getContext(), "Emergency services notified", Toast.LENGTH_LONG).show();
                        }
                    }
                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        Toast.makeText(getContext(), "Failed to send panic alert", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void triggerStop() {
        if (stopTriggered) return;
        GeoPoint pos = vehicleMarker != null ? vehicleMarker.getPosition() : new GeoPoint(pickupLat, pickupLng);
        RetrofitClient.getClient(requireContext())
                .create(RideService.class)
                .stopRide(new StopRideRequest(pos.getLatitude(), pos.getLongitude()))
                .enqueue(new Callback<StopRideResponse>() {
                    @Override
                    public void onResponse(Call<StopRideResponse> call, Response<StopRideResponse> response) {
                        if (response.isSuccessful()) {
                            stopTriggered = true;
                            stopSimulation();
                            btnStopRide.setEnabled(false);
                            btnStopRide.setText("Ride Stopped");
                            requireActivity().getSupportFragmentManager().popBackStack();
                        } else {
                            Toast.makeText(getContext(), "Failed to stop ride", Toast.LENGTH_SHORT).show();
                        }
                    }
                    @Override
                    public void onFailure(Call<StopRideResponse> call, Throwable t) {
                        Toast.makeText(getContext(), "Network error", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showCompletionDialog() {
        DriverCompleteRideDialogFragment.newInstance(
                rideId, totalDistance, totalDurationMin,
                calculatePrice(), pickupAddress, dropoffAddress
        ).show(requireActivity().getSupportFragmentManager(), "ride_complete");
    }*/
}