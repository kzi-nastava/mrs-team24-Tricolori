package com.example.mobile.ui.fragments;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.mobile.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

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
import java.util.Random;

public class HomeFragment extends Fragment {

    private static final String TAG = "HomeFragment";
    private MapView mapView;
    private TextView tvAvailableCount;
    private TextView tvBusyCount;
    private LinearLayout llAuthButtons;
    private LinearLayout llEstimationContent;
    private LinearLayout llEstimationResults;
    private ImageView ivExpandIcon;
    private TextInputEditText etStartLocation;
    private TextInputEditText etEndLocation;
    private TextView tvDistance;
    private TextView tvDuration;
    private TextView tvEstimatedPrice;
    private SharedPreferences sharedPreferences;
    private Handler updateHandler;
    private Runnable updateRunnable;
    private List<VehicleMarker> vehicleMarkers;
    private boolean isEstimationExpanded = false;
    private Polyline routeOverlay;

    // Default location (Novi Sad, Serbia)
    private static final double DEFAULT_LAT = 45.2671;
    private static final double DEFAULT_LON = 19.8335;
    private static final int DEFAULT_ZOOM = 13;
    private static final double PRICE_PER_KM = 120.0; // RSD per km

    public HomeFragment() {
        // Required empty constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize OSMDroid configuration
        Context ctx = requireContext();
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));
        Configuration.getInstance().setUserAgentValue(ctx.getPackageName());

        // Initialize SharedPreferences
        sharedPreferences = requireActivity().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);

        // Initialize views
        initializeViews(view);

        // Initialize map
        initializeMap(view);

        // Check if user is logged in
        boolean isLoggedIn = sharedPreferences.getBoolean("is_logged_in", false);
        llAuthButtons.setVisibility(isLoggedIn ? View.GONE : View.VISIBLE);

        // Set up listeners
        setupListeners(view);

        // Initialize vehicle markers list
        vehicleMarkers = new ArrayList<>();

        // Load initial vehicles
        loadVehicles();

        // Set up periodic updates (every 30 seconds for simulation)
        setupPeriodicUpdates();
    }

    private void initializeViews(View view) {
        tvAvailableCount = view.findViewById(R.id.tvAvailableCount);
        tvBusyCount = view.findViewById(R.id.tvBusyCount);
        llAuthButtons = view.findViewById(R.id.llAuthButtons);
        llEstimationContent = view.findViewById(R.id.llEstimationContent);
        llEstimationResults = view.findViewById(R.id.llEstimationResults);
        ivExpandIcon = view.findViewById(R.id.ivExpandIcon);
        etStartLocation = view.findViewById(R.id.etStartLocation);
        etEndLocation = view.findViewById(R.id.etEndLocation);
        tvDistance = view.findViewById(R.id.tvDistance);
        tvDuration = view.findViewById(R.id.tvDuration);
        tvEstimatedPrice = view.findViewById(R.id.tvEstimatedPrice);
    }

    private void setupListeners(View view) {
        MaterialButton btnLogin = view.findViewById(R.id.btnLogin);
        MaterialButton btnRegister = view.findViewById(R.id.btnRegister);
        FloatingActionButton fabRefresh = view.findViewById(R.id.fabRefresh);
        LinearLayout llEstimationHeader = view.findViewById(R.id.llEstimationHeader);
        MaterialButton btnCalculateRoute = view.findViewById(R.id.btnCalculateRoute);

        btnLogin.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_home_to_login));

        btnRegister.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_home_to_register));

        fabRefresh.setOnClickListener(v -> {
            refreshVehicles();
            Toast.makeText(getContext(), R.string.refreshing_vehicles, Toast.LENGTH_SHORT).show();
        });

        llEstimationHeader.setOnClickListener(v -> toggleEstimationPanel());

        btnCalculateRoute.setOnClickListener(v -> calculateRoute());
    }

    private void initializeMap(View view) {
        mapView = new MapView(requireContext());
        mapView.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));

        ViewGroup mapContainer = view.findViewById(R.id.mapContainer);
        mapContainer.addView(mapView, 0);

        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setBuiltInZoomControls(true);
        mapView.setMultiTouchControls(true);

        // Set initial position
        mapView.getController().setZoom(DEFAULT_ZOOM);
        mapView.getController().setCenter(new GeoPoint(DEFAULT_LAT, DEFAULT_LON));
    }

    private void toggleEstimationPanel() {
        isEstimationExpanded = !isEstimationExpanded;

        if (isEstimationExpanded) {
            llEstimationContent.setVisibility(View.VISIBLE);
            ObjectAnimator.ofFloat(ivExpandIcon, "rotation", 0f, 180f).setDuration(300).start();
        } else {
            llEstimationContent.setVisibility(View.GONE);
            ObjectAnimator.ofFloat(ivExpandIcon, "rotation", 180f, 0f).setDuration(300).start();
        }
    }

    private void calculateRoute() {
        String startLocation = etStartLocation.getText().toString().trim();
        String endLocation = etEndLocation.getText().toString().trim();

        if (startLocation.isEmpty() || endLocation.isEmpty()) {
            Toast.makeText(getContext(), R.string.error_empty_locations, Toast.LENGTH_SHORT).show();
            return;
        }

        // Show loading indicator
        Toast.makeText(getContext(), R.string.calculating_route, Toast.LENGTH_SHORT).show();

        // Run route calculation in background thread
        new Thread(() -> {
            try {
                // Geocode addresses to coordinates
                GeoPoint startPoint = geocodeAddress(startLocation);
                GeoPoint endPoint = geocodeAddress(endLocation);

                if (startPoint == null || endPoint == null) {
                    requireActivity().runOnUiThread(() ->
                            Toast.makeText(getContext(), R.string.error_geocoding, Toast.LENGTH_SHORT).show());
                    return;
                }

                // Calculate route using OSRM
                RoadManager roadManager = new OSRMRoadManager(requireContext(),
                        Configuration.getInstance().getUserAgentValue());

                ArrayList<GeoPoint> waypoints = new ArrayList<>();
                waypoints.add(startPoint);
                waypoints.add(endPoint);

                Road road = roadManager.getRoad(waypoints);

                // Update UI on main thread
                requireActivity().runOnUiThread(() -> {
                    if (road != null && road.mStatus == Road.STATUS_OK) {
                        displayRouteResults(road);
                        drawRouteOnMap(road, startPoint, endPoint);
                    } else {
                        Toast.makeText(getContext(), R.string.error_route_calculation,
                                Toast.LENGTH_SHORT).show();
                    }
                });

            } catch (Exception e) {
                Log.e(TAG, "Error calculating route", e);
                requireActivity().runOnUiThread(() ->
                        Toast.makeText(getContext(), R.string.error_route_calculation,
                                Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private GeoPoint geocodeAddress(String address) {
        // Simple geocoding for Novi Sad locations
        // In production, use Nominatim or another geocoding service

        // Check for some common Novi Sad locations
        String lowerAddress = address.toLowerCase();

        // City center and main squares
        if (lowerAddress.contains("trg slobode") || lowerAddress.contains("freedom square")) {
            return new GeoPoint(45.2671, 19.8335);
        } else if (lowerAddress.contains("zmaj jovina") || lowerAddress.contains("zmaj jovina ulica")) {
            return new GeoPoint(45.2551, 19.8451);
        } else if (lowerAddress.contains("dunavska") || lowerAddress.contains("dunavska ulica")) {
            return new GeoPoint(45.2558, 19.8468);
        }

        // Notable landmarks
        else if (lowerAddress.contains("petrovaradin") || lowerAddress.contains("tvrđava") || lowerAddress.contains("fortress")) {
            return new GeoPoint(45.2516, 19.8661);
        } else if (lowerAddress.contains("štrand") || lowerAddress.contains("strand")) {
            return new GeoPoint(45.2398, 19.8425);
        } else if (lowerAddress.contains("spens") || lowerAddress.contains("master")) {
            return new GeoPoint(45.2444, 19.8361);
        } else if (lowerAddress.contains("sajam") || lowerAddress.contains("novosadski sajam")) {
            return new GeoPoint(45.2447, 19.8084);
        }

        // Shopping centers
        else if (lowerAddress.contains("big") || lowerAddress.contains("mercator")) {
            return new GeoPoint(45.2530, 19.8312);
        } else if (lowerAddress.contains("promenada")) {
            return new GeoPoint(45.2639, 19.8319);
        } else if (lowerAddress.contains("aviv") || lowerAddress.contains("aviv park")) {
            return new GeoPoint(45.2708, 19.8044);
        }

        // Universities
        else if (lowerAddress.contains("univerzitet") || lowerAddress.contains("university") || lowerAddress.contains("rektorat")) {
            return new GeoPoint(45.2479, 19.8517);
        } else if (lowerAddress.contains("ftn") || lowerAddress.contains("tehnički fakultet")) {
            return new GeoPoint(45.2468, 19.8517);
        }

        // Train and bus stations
        else if (lowerAddress.contains("železnička") || lowerAddress.contains("train station") || lowerAddress.contains("stanica")) {
            return new GeoPoint(45.2674, 19.8433);
        } else if (lowerAddress.contains("autobuska") || lowerAddress.contains("bus station")) {
            return new GeoPoint(45.2540, 19.8363);
        }

        // Neighborhoods
        else if (lowerAddress.contains("liman")) {
            return new GeoPoint(45.2391, 19.8255);
        } else if (lowerAddress.contains("grbavica")) {
            return new GeoPoint(45.2334, 19.8420);
        } else if (lowerAddress.contains("podbara")) {
            return new GeoPoint(45.2614, 19.8151);
        } else if (lowerAddress.contains("novo naselje")) {
            return new GeoPoint(45.2800, 19.8200);
        } else if (lowerAddress.contains("detelinara")) {
            return new GeoPoint(45.2750, 19.8550);
        } else if (lowerAddress.contains("telep")) {
            return new GeoPoint(45.2450, 19.8050);
        }

        // Default: add small random offset from city center for demo
        Random random = new Random(address.hashCode());
        double lat = DEFAULT_LAT + (random.nextDouble() - 0.5) * 0.03;
        double lon = DEFAULT_LON + (random.nextDouble() - 0.5) * 0.03;
        return new GeoPoint(lat, lon);
    }

    private void displayRouteResults(Road road) {
        // Distance in km
        double distanceKm = road.mLength;
        tvDistance.setText(String.format("%.2f km", distanceKm));

        // Duration in minutes and seconds (as per requirements)
        double durationMinutes = road.mDuration / 60.0;
        int minutes = (int) durationMinutes;
        int seconds = (int) ((durationMinutes - minutes) * 60);
        tvDuration.setText(String.format("%d min %d sec", minutes, seconds));

        // Estimated price
        double estimatedPrice = distanceKm * PRICE_PER_KM;
        tvEstimatedPrice.setText(String.format("%.0f RSD", estimatedPrice));

        // Show results
        llEstimationResults.setVisibility(View.VISIBLE);
    }

    private void drawRouteOnMap(Road road, GeoPoint startPoint, GeoPoint endPoint) {
        // Remove existing route overlay
        if (routeOverlay != null) {
            mapView.getOverlays().remove(routeOverlay);
        }

        // Create route polyline
        routeOverlay = RoadManager.buildRoadOverlay(road);
        routeOverlay.getOutlinePaint().setColor(Color.parseColor("#00a2ff"));
        routeOverlay.getOutlinePaint().setStrokeWidth(12f);
        mapView.getOverlays().add(routeOverlay);

        // Add start marker
        Marker startMarker = new Marker(mapView);
        startMarker.setPosition(startPoint);
        startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        startMarker.setTitle(getString(R.string.start_location));
        Drawable startIcon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_start_marker);
        startMarker.setIcon(startIcon);
        mapView.getOverlays().add(startMarker);

        // Add end marker
        Marker endMarker = new Marker(mapView);
        endMarker.setPosition(endPoint);
        endMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        endMarker.setTitle(getString(R.string.end_location));
        Drawable endIcon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_end_marker);
        endMarker.setIcon(endIcon);
        mapView.getOverlays().add(endMarker);

        // Zoom to show entire route
        mapView.zoomToBoundingBox(road.mBoundingBox, true);

        mapView.invalidate();
    }

    private void loadVehicles() {
        // TODO: Replace with actual API call to fetch vehicles
        // For now, generate mock data
        generateMockVehicles();
        updateVehicleCounts();
    }

    private void generateMockVehicles() {
        // Clear existing markers
        for (VehicleMarker vm : vehicleMarkers) {
            mapView.getOverlays().remove(vm.marker);
        }
        vehicleMarkers.clear();

        Random random = new Random();
        int numVehicles = 8 + random.nextInt(5); // 8-12 vehicles

        for (int i = 0; i < numVehicles; i++) {
            // Generate random position near center
            double lat = DEFAULT_LAT + (random.nextDouble() - 0.5) * 0.05;
            double lon = DEFAULT_LON + (random.nextDouble() - 0.5) * 0.05;
            boolean isAvailable = random.nextBoolean();

            VehicleMarker vehicleMarker = new VehicleMarker(
                    "Vehicle " + (i + 1),
                    lat,
                    lon,
                    isAvailable
            );

            Marker marker = new Marker(mapView);
            marker.setPosition(new GeoPoint(lat, lon));
            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
            marker.setTitle(vehicleMarker.name);
            marker.setSnippet(isAvailable ?
                    getString(R.string.status_available) :
                    getString(R.string.status_busy));

            // Set marker icon based on availability
            Drawable icon = ContextCompat.getDrawable(requireContext(),
                    isAvailable ? R.drawable.ic_vehicle_available : R.drawable.ic_vehicle_busy);
            marker.setIcon(icon);

            vehicleMarker.marker = marker;
            vehicleMarkers.add(vehicleMarker);
            mapView.getOverlays().add(marker);
        }

        mapView.invalidate();
    }

    private void updateVehicleCounts() {
        int availableCount = 0;
        int busyCount = 0;

        for (VehicleMarker vm : vehicleMarkers) {
            if (vm.isAvailable) {
                availableCount++;
            } else {
                busyCount++;
            }
        }

        tvAvailableCount.setText(getString(R.string.available_count, availableCount));
        tvBusyCount.setText(getString(R.string.busy_count, busyCount));
    }

    private void refreshVehicles() {
        loadVehicles();
    }

    private void setupPeriodicUpdates() {
        updateHandler = new Handler(Looper.getMainLooper());
        updateRunnable = new Runnable() {
            @Override
            public void run() {
                // Simulate vehicle movement and status changes
                simulateVehicleUpdates();
                updateHandler.postDelayed(this, 30000); // Update every 30 seconds
            }
        };
        updateHandler.postDelayed(updateRunnable, 30000);
    }

    private void simulateVehicleUpdates() {
        Random random = new Random();

        for (VehicleMarker vm : vehicleMarkers) {
            // Randomly change availability (10% chance)
            if (random.nextInt(10) == 0) {
                vm.isAvailable = !vm.isAvailable;
                vm.marker.setSnippet(vm.isAvailable ?
                        getString(R.string.status_available) :
                        getString(R.string.status_busy));

                Drawable icon = ContextCompat.getDrawable(requireContext(),
                        vm.isAvailable ? R.drawable.ic_vehicle_available : R.drawable.ic_vehicle_busy);
                vm.marker.setIcon(icon);
            }

            // Simulate small position changes (only if available)
            if (vm.isAvailable) {
                GeoPoint currentPos = vm.marker.getPosition();
                double newLat = currentPos.getLatitude() + (random.nextDouble() - 0.5) * 0.002;
                double newLon = currentPos.getLongitude() + (random.nextDouble() - 0.5) * 0.002;
                vm.marker.setPosition(new GeoPoint(newLat, newLon));
            }
        }

        updateVehicleCounts();
        mapView.invalidate();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mapView != null) {
            mapView.onResume();
        }

        // Check if user logged in/out
        boolean isLoggedIn = sharedPreferences.getBoolean("is_logged_in", false);
        if (llAuthButtons != null) {
            llAuthButtons.setVisibility(isLoggedIn ? View.GONE : View.VISIBLE);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mapView != null) {
            mapView.onPause();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (updateHandler != null && updateRunnable != null) {
            updateHandler.removeCallbacks(updateRunnable);
        }
    }

    // Inner class to hold vehicle data
    private static class VehicleMarker {
        String name;
        double latitude;
        double longitude;
        boolean isAvailable;
        Marker marker;

        VehicleMarker(String name, double lat, double lon, boolean available) {
            this.name = name;
            this.latitude = lat;
            this.longitude = lon;
            this.isAvailable = available;
        }
    }
}