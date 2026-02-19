package com.example.mobile.ui.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mobile.R;
import com.example.mobile.dto.ride.RideTrackingResponse;
import com.example.mobile.network.RetrofitClient;
import com.example.mobile.network.service.RideService;
import com.example.mobile.ui.components.MapComponent;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminSuperviseFragment extends Fragment {

    // Views
    private MapView mapView;
    private MapComponent mapComponent;
    private RecyclerView driverRecyclerView;
    private EditText searchInput;
    private TextView activeRidesCount;
    private LinearLayout driverListPanel;
    private LinearLayout driverDetailPanel;
    private LinearLayout emptyStateView;

    // Detail panel views
    private TextView detailDriverName;
    private TextView detailVehicleType;
    private TextView detailLicensePlate;
    private TextView detailRideId;
    private TextView detailPassenger;
    private TextView detailPickup;
    private TextView detailDestination;
    private TextView detailDeparture;
    private TextView detailArrival;
    private TextView detailDistance;
    private TextView detailPrice;
    private ProgressBar detailProgress;
    private TextView detailProgressText;

    // Data
    private List<RideTrackingResponse> allRides = new ArrayList<>();
    private List<RideTrackingResponse> filteredRides = new ArrayList<>();
    private RideTrackingResponse selectedRide = null;
    private DriverCardAdapter adapter;

    // Live updates
    private final Handler updateHandler = new Handler(Looper.getMainLooper());
    private static final int UPDATE_INTERVAL_MS = 5000;
    private RideService rideService;

    // Driver marker on map
    private Marker driverMarker;

    private final Runnable updateRunnable = new Runnable() {
        @Override
        public void run() {
            loadOngoingRides();
            updateHandler.postDelayed(this, UPDATE_INTERVAL_MS);
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_admin_supervise, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rideService = RetrofitClient.getClient(requireContext()).create(RideService.class);

        initMap(view);
        initViews(view);
        setupSearch();
        setupAdapter();

        loadOngoingRides();
        startLiveUpdates();
    }

    private void initMap(View view) {
        Configuration.getInstance().setUserAgentValue(requireContext().getPackageName());
        mapView = view.findViewById(R.id.supervise_map);
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setMultiTouchControls(true);
        mapView.getController().setZoom(13.0);
        mapView.getController().setCenter(new GeoPoint(45.2671, 19.8335));
        mapComponent = new MapComponent(mapView, requireContext());
    }

    private void initViews(View view) {
        searchInput = view.findViewById(R.id.search_input);
        activeRidesCount = view.findViewById(R.id.active_rides_count);
        driverRecyclerView = view.findViewById(R.id.driver_recycler_view);
        driverListPanel = view.findViewById(R.id.driver_list_panel);
        driverDetailPanel = view.findViewById(R.id.driver_detail_panel);
        emptyStateView = view.findViewById(R.id.empty_state_view);

        detailDriverName = view.findViewById(R.id.detail_driver_name);
        detailVehicleType = view.findViewById(R.id.detail_vehicle_type);
        detailLicensePlate = view.findViewById(R.id.detail_license_plate);
        detailRideId = view.findViewById(R.id.detail_ride_id);
        detailPassenger = view.findViewById(R.id.detail_passenger);
        detailPickup = view.findViewById(R.id.detail_pickup);
        detailDestination = view.findViewById(R.id.detail_destination);
        detailDeparture = view.findViewById(R.id.detail_departure);
        detailArrival = view.findViewById(R.id.detail_arrival);
        detailDistance = view.findViewById(R.id.detail_distance);
        detailPrice = view.findViewById(R.id.detail_price);
        detailProgress = view.findViewById(R.id.detail_progress_bar);
        detailProgressText = view.findViewById(R.id.detail_progress_text);

        view.findViewById(R.id.btn_back).setOnClickListener(v -> deselectDriver());
    }

    private void setupSearch() {
        searchInput.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterDrivers(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private void setupAdapter() {
        adapter = new DriverCardAdapter(filteredRides, this::selectDriver);
        driverRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        driverRecyclerView.setAdapter(adapter);
    }

    private void loadOngoingRides() {
        rideService.getAllOngoingRides().enqueue(new Callback<List<RideTrackingResponse>>() {
            @Override
            public void onResponse(@NonNull Call<List<RideTrackingResponse>> call,
                                   @NonNull Response<List<RideTrackingResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    allRides = response.body();
                    activeRidesCount.setText(String.valueOf(allRides.size()));
                    filterDrivers(searchInput.getText().toString());

                    if (selectedRide != null) {
                        for (RideTrackingResponse ride : allRides) {
                            if (ride.getRideId().equals(selectedRide.getRideId())) {
                                selectedRide = ride;
                                populateDetailPanel(ride);
                                updateDriverMarker(ride);
                                break;
                            }
                        }
                    }

                    updateAllDriverMarkers();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<RideTrackingResponse>> call, @NonNull Throwable t) {
                // Silently fail on background refresh
            }
        });
    }

    private void filterDrivers(String query) {
        filteredRides.clear();
        if (query == null || query.trim().isEmpty()) {
            filteredRides.addAll(allRides);
        } else {
            String lower = query.toLowerCase();
            for (RideTrackingResponse ride : allRides) {
                String driverName = getDriverName(ride).toLowerCase();
                String plate = ride.getCurrentLocation() != null
                        ? ride.getCurrentLocation().getPlateNum() : "";
                if (driverName.contains(lower) || (plate != null && plate.toLowerCase().contains(lower))) {
                    filteredRides.add(ride);
                }
            }
        }
        adapter.notifyDataSetChanged();
        emptyStateView.setVisibility(filteredRides.isEmpty() ? View.VISIBLE : View.GONE);
        driverRecyclerView.setVisibility(filteredRides.isEmpty() ? View.GONE : View.VISIBLE);
    }

    private void selectDriver(RideTrackingResponse ride) {
        selectedRide = ride;
        driverListPanel.setVisibility(View.GONE);
        driverDetailPanel.setVisibility(View.VISIBLE);
        populateDetailPanel(ride);
        focusMapOnDriver(ride);
    }

    private void deselectDriver() {
        selectedRide = null;
        driverDetailPanel.setVisibility(View.GONE);
        driverListPanel.setVisibility(View.VISIBLE);
        mapComponent.clearRouteAndMarkers();
        updateAllDriverMarkers();
    }

    private void populateDetailPanel(RideTrackingResponse ride) {
        detailDriverName.setText(getDriverName(ride));
        detailVehicleType.setText(ride.getCurrentLocation() != null
                ? ride.getCurrentLocation().getModel() : "Unknown");
        detailLicensePlate.setText(ride.getCurrentLocation() != null
                ? ride.getCurrentLocation().getPlateNum() : "N/A");

        detailRideId.setText("#" + ride.getRideId());
        detailPassenger.setText(getPassengerName(ride));

        if (ride.getRoute() != null) {
            detailPickup.setText(ride.getRoute().getPickupAddress());
            detailDestination.setText(ride.getRoute().getDestinationAddress());
            detailDistance.setText(String.format(Locale.getDefault(), "%.1f km",
                    ride.getRoute().getDistanceKm()));
        }

        detailDeparture.setText(formatTime(ride.getStartTime()));
        detailArrival.setText(formatTime(ride.getEstimatedArrival()));
        detailPrice.setText(String.format(Locale.getDefault(), "%.2f RSD",
                ride.getPrice() != null ? ride.getPrice() : 0.0));

        int progress = calculateProgress(ride);
        detailProgress.setProgress(progress);
        detailProgressText.setText(progress + "%");
    }

    private void focusMapOnDriver(RideTrackingResponse ride) {
        if (ride.getCurrentLocation() != null) {
            GeoPoint driverPos = new GeoPoint(
                    ride.getCurrentLocation().getLatitude(),
                    ride.getCurrentLocation().getLongitude()
            );
            mapView.getController().animateTo(driverPos);
            mapView.getController().setZoom(15.0);
        }

        if (ride.getRoute() != null) {
            GeoPoint pickup = new GeoPoint(ride.getRoute().getPickupLatitude(),
                    ride.getRoute().getPickupLongitude());
            GeoPoint dest = new GeoPoint(ride.getRoute().getDestinationLatitude(),
                    ride.getRoute().getDestinationLongitude());
            List<GeoPoint> points = new ArrayList<>();
            points.add(pickup);
            if (ride.getCurrentLocation() != null) {
                points.add(1, new GeoPoint(ride.getCurrentLocation().getLatitude(),
                        ride.getCurrentLocation().getLongitude()));
            }
            points.add(dest);
            mapComponent.drawRoute(points);
        }

        updateDriverMarker(ride);
    }

    private void updateDriverMarker(RideTrackingResponse ride) {
        if (ride.getCurrentLocation() == null) return;
        GeoPoint pos = new GeoPoint(
                ride.getCurrentLocation().getLatitude(),
                ride.getCurrentLocation().getLongitude()
        );
        if (driverMarker == null) {
            driverMarker = new Marker(mapView);
            driverMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER);
            mapView.getOverlays().add(driverMarker);
        }
        driverMarker.setPosition(pos);
        driverMarker.setTitle(getDriverName(ride));
        mapView.invalidate();
    }

    private void updateAllDriverMarkers() {
        if (selectedRide != null) return;
        mapComponent.clearRouteAndMarkers();
    }

    // --- Helpers ---

    private String getDriverName(RideTrackingResponse ride) {
        if (ride.getDriver() == null) return "Unknown Driver";
        return ride.getDriver().getFirstName() + " " + ride.getDriver().getLastName();
    }

    private String getPassengerName(RideTrackingResponse ride) {
        if (ride.getPassengers() == null || ride.getPassengers().isEmpty()) return "Unknown";
        // Uses the updated PassengerDto inner class from RideTrackingResponse
        RideTrackingResponse.PassengerDto p = ride.getPassengers().get(0);
        return p.getFirstName() + " " + p.getLastName();
    }

    private int calculateProgress(RideTrackingResponse ride) {
        if (ride.getStartTime() == null || ride.getEstimatedTimeMinutes() == null
                || ride.getEstimatedTimeMinutes() == 0) return 0;
        try {
            long startMs = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                    .parse(ride.getStartTime()).getTime();
            long totalMs = ride.getEstimatedTimeMinutes() * 60_000L;
            long elapsedMs = System.currentTimeMillis() - startMs;
            return (int) Math.min(100, Math.max(0, (elapsedMs * 100) / totalMs));
        } catch (Exception e) {
            return 0;
        }
    }

    private String formatTime(String isoString) {
        if (isoString == null) return "N/A";
        try {
            Date d = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).parse(isoString);
            return new SimpleDateFormat("HH:mm", Locale.getDefault()).format(d);
        } catch (Exception e) {
            return "N/A";
        }
    }

    private void startLiveUpdates() {
        updateHandler.postDelayed(updateRunnable, UPDATE_INTERVAL_MS);
    }

    private void stopLiveUpdates() {
        updateHandler.removeCallbacks(updateRunnable);
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        stopLiveUpdates();
        mapView.onDetach();
    }

    // ---- Inner Adapter ----

    interface OnDriverClickListener {
        void onClick(RideTrackingResponse ride);
    }

    static class DriverCardAdapter extends RecyclerView.Adapter<DriverCardAdapter.ViewHolder> {
        private final List<RideTrackingResponse> rides;
        private final OnDriverClickListener listener;

        DriverCardAdapter(List<RideTrackingResponse> rides, OnDriverClickListener listener) {
            this.rides = rides;
            this.listener = listener;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_driver_card, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            RideTrackingResponse ride = rides.get(position);
            String driverName = ride.getDriver() != null
                    ? ride.getDriver().getFirstName() + " " + ride.getDriver().getLastName()
                    : "Unknown Driver";
            holder.driverName.setText(driverName);
            holder.vehicleType.setText(ride.getCurrentLocation() != null
                    ? ride.getCurrentLocation().getModel() : "Unknown Vehicle");
            holder.licensePlate.setText(ride.getCurrentLocation() != null
                    ? ride.getCurrentLocation().getPlateNum() : "N/A");

            if (ride.getRoute() != null) {
                holder.pickup.setText(ride.getRoute().getPickupAddress());
                holder.destination.setText(ride.getRoute().getDestinationAddress());
                holder.routeInfo.setVisibility(View.VISIBLE);
            } else {
                holder.routeInfo.setVisibility(View.GONE);
            }

            holder.itemView.setOnClickListener(v -> listener.onClick(ride));
        }

        @Override
        public int getItemCount() { return rides.size(); }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView driverName, vehicleType, licensePlate, pickup, destination;
            View routeInfo;
            ViewHolder(View v) {
                super(v);
                driverName = v.findViewById(R.id.card_driver_name);
                vehicleType = v.findViewById(R.id.card_vehicle_type);
                licensePlate = v.findViewById(R.id.card_license_plate);
                pickup = v.findViewById(R.id.card_pickup);
                destination = v.findViewById(R.id.card_destination);
                routeInfo = v.findViewById(R.id.card_route_info);
            }
        }
    }
}