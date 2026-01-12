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
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
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
    private Marker startMarker;
    private Marker endMarker;

    // Default location (Novi Sad, Serbia)
    private static final double DEFAULT_LAT = 45.2671;
    private static final double DEFAULT_LON = 19.8335;
    private static final int DEFAULT_ZOOM = 13;
    private static final double PRICE_PER_KM = 120.0; // RSD per km

    // Predefined road locations in Novi Sad (major streets and intersections)
    private static final GeoPoint[] ROAD_LOCATIONS = {
            // City Center
            new GeoPoint(45.2671, 19.8335), // Trg Slobode
            new GeoPoint(45.2551, 19.8451), // Zmaj Jovina
            new GeoPoint(45.2558, 19.8468), // Dunavska
            new GeoPoint(45.2650, 19.8320), // Bulevar Oslobođenja
            new GeoPoint(45.2620, 19.8380), // Bulevar Mihajla Pupina
            new GeoPoint(45.2590, 19.8410), // Kralja Aleksandra
            new GeoPoint(45.2540, 19.8360), // Autobuska Stanica
            new GeoPoint(45.2674, 19.8433), // Železnička Stanica

            // Petrovaradin area (fortress side)
            new GeoPoint(45.2516, 19.8661), // Petrovaradin Fortress
            new GeoPoint(45.2505, 19.8640), // Petrovaradin main road
            new GeoPoint(45.2490, 19.8620), // Near fortress entrance

            // Universities and institutions
            new GeoPoint(45.2479, 19.8517), // University Campus
            new GeoPoint(45.2468, 19.8517), // FTN
            new GeoPoint(45.2460, 19.8500), // Campus road

            // Shopping areas
            new GeoPoint(45.2530, 19.8312), // BIG Shopping Center
            new GeoPoint(45.2639, 19.8319), // Promenada
            new GeoPoint(45.2708, 19.8044), // Aviv Park
            new GeoPoint(45.2447, 19.8084), // Novosadski Sajam

            // Sports centers
            new GeoPoint(45.2444, 19.8361), // SPENS
            new GeoPoint(45.2398, 19.8425), // Štrand beach area

            // Liman neighborhood (west side)
            new GeoPoint(45.2391, 19.8255), // Liman 1
            new GeoPoint(45.2360, 19.8220), // Liman 2
            new GeoPoint(45.2330, 19.8180), // Liman 3
            new GeoPoint(45.2300, 19.8150), // Liman 4

            // Grbavica (south side)
            new GeoPoint(45.2334, 19.8420), // Grbavica center
            new GeoPoint(45.2310, 19.8440), // Grbavica south
            new GeoPoint(45.2290, 19.8400), // Grbavica west

            // Novo Naselje (north side)
            new GeoPoint(45.2800, 19.8200), // Novo Naselje center
            new GeoPoint(45.2820, 19.8180), // Novo Naselje north
            new GeoPoint(45.2780, 19.8220), // Novo Naselje east

            // Detelinara (northeast)
            new GeoPoint(45.2750, 19.8550), // Detelinara center
            new GeoPoint(45.2770, 19.8570), // Detelinara north
            new GeoPoint(45.2730, 19.8530), // Detelinara south

            // Telep (west)
            new GeoPoint(45.2450, 19.8050), // Telep center
            new GeoPoint(45.2470, 19.8030), // Telep north
            new GeoPoint(45.2430, 19.8070), // Telep south

            // Podbara (northwest)
            new GeoPoint(45.2614, 19.8151), // Podbara center
            new GeoPoint(45.2630, 19.8130), // Podbara north
            new GeoPoint(45.2590, 19.8170), // Podbara south

            // Major roads and boulevards
            new GeoPoint(45.2700, 19.8250), // Bulevar Cara Lazara
            new GeoPoint(45.2580, 19.8200), // Narodnih Heroja
            new GeoPoint(45.2520, 19.8280), // Janka Veselinovića
            new GeoPoint(45.2600, 19.8500), // Sentandrejski put
            new GeoPoint(45.2420, 19.8300), // Rumenačka
            new GeoPoint(45.2680, 19.8450), // Futoski put
    };

    public HomeFragment() {
        // Required empty constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (getActivity() != null) {
            AppCompatActivity activity = (AppCompatActivity) getActivity();
            if (activity.getSupportActionBar() != null) {
                activity.getSupportActionBar().setDisplayHomeAsUpEnabled(false);
                activity.getSupportActionBar().setTitle("Cuber");
            }
        }

        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Context ctx = requireContext();
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));
        Configuration.getInstance().setUserAgentValue(ctx.getPackageName());

        sharedPreferences = requireActivity().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);

        initializeViews(view);
        initializeMap(view);

        boolean isLoggedIn = sharedPreferences.getBoolean("is_logged_in", false);
        llAuthButtons.setVisibility(isLoggedIn ? View.GONE : View.VISIBLE);

        setupListeners(view);

        vehicleMarkers = new ArrayList<>();

        loadVehicles();
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
        MaterialButton btnGetStarted = view.findViewById(R.id.btnGetStarted);
        FloatingActionButton fabRefresh = view.findViewById(R.id.fabRefresh);
        LinearLayout llEstimationHeader = view.findViewById(R.id.llEstimationHeader);
        MaterialButton btnCalculateRoute = view.findViewById(R.id.btnCalculateRoute);

        if (btnGetStarted != null) {
            btnGetStarted.setOnClickListener(v -> {
                Log.d(TAG, "Get Started button clicked!");
                Navigation.findNavController(v).navigate(R.id.action_home_to_login);
            });
        }

        if (fabRefresh != null) {
            fabRefresh.setOnClickListener(v -> {
                Log.d(TAG, "Refresh button clicked!");
                refreshVehicles();
                Toast.makeText(getContext(), R.string.refreshing_vehicles, Toast.LENGTH_SHORT).show();
            });
        }

        if (llEstimationHeader != null) {
            llEstimationHeader.setOnClickListener(v -> toggleEstimationPanel());
        }

        if (btnCalculateRoute != null) {
            btnCalculateRoute.setOnClickListener(v -> calculateRoute());
        }
    }

    private void initializeMap(View view) {
        ViewGroup mapContainer = view.findViewById(R.id.mapContainer);

        mapView = new MapView(requireContext());
        mapView.setLayoutParams(new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT));

        mapContainer.addView(mapView, 0);

        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setBuiltInZoomControls(true);
        mapView.setMultiTouchControls(true);

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

        hideKeyboard();
        Toast.makeText(getContext(), R.string.calculating_route, Toast.LENGTH_SHORT).show();

        new Thread(() -> {
            try {
                GeoPoint startPoint = geocodeAddress(startLocation);
                GeoPoint endPoint = geocodeAddress(endLocation);

                if (startPoint == null || endPoint == null) {
                    requireActivity().runOnUiThread(() ->
                            Toast.makeText(getContext(), R.string.error_geocoding, Toast.LENGTH_SHORT).show());
                    return;
                }

                RoadManager roadManager = new OSRMRoadManager(requireContext(),
                        Configuration.getInstance().getUserAgentValue());

                ArrayList<GeoPoint> waypoints = new ArrayList<>();
                waypoints.add(startPoint);
                waypoints.add(endPoint);

                Road road = roadManager.getRoad(waypoints);

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

    private void hideKeyboard() {
        if (getActivity() != null && getView() != null) {
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);
        }
    }

    private GeoPoint geocodeAddress(String address) {
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

        // Default: return a random road location
        return getRandomRoadLocation();
    }

    /**
     * Get a random location from predefined road locations
     */
    private GeoPoint getRandomRoadLocation() {
        Random random = new Random();
        int index = random.nextInt(ROAD_LOCATIONS.length);
        return ROAD_LOCATIONS[index];
    }

    /**
     * Get a nearby road location (for vehicle movement simulation)
     */
    private GeoPoint getNearbyRoadLocation(GeoPoint currentLocation) {
        Random random = new Random();

        // Find road locations within ~500m
        List<GeoPoint> nearbyLocations = new ArrayList<>();
        for (GeoPoint roadPoint : ROAD_LOCATIONS) {
            double distance = calculateDistance(currentLocation, roadPoint);
            if (distance < 0.5) { // within 500 meters
                nearbyLocations.add(roadPoint);
            }
        }

        // If we found nearby locations, pick one randomly
        if (!nearbyLocations.isEmpty()) {
            int index = random.nextInt(nearbyLocations.size());
            return nearbyLocations.get(index);
        }

        // Otherwise, just pick any road location
        return getRandomRoadLocation();
    }

    /**
     * Calculate distance between two points in kilometers (Haversine formula)
     */
    private double calculateDistance(GeoPoint point1, GeoPoint point2) {
        double lat1 = Math.toRadians(point1.getLatitude());
        double lon1 = Math.toRadians(point1.getLongitude());
        double lat2 = Math.toRadians(point2.getLatitude());
        double lon2 = Math.toRadians(point2.getLongitude());

        double dLat = lat2 - lat1;
        double dLon = lon2 - lon1;

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(lat1) * Math.cos(lat2) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return 6371 * c; // Earth radius in km
    }

    private void displayRouteResults(Road road) {
        double distanceKm = road.mLength;
        tvDistance.setText(String.format("%.2f km", distanceKm));

        double durationMinutes = road.mDuration / 60.0;
        int minutes = (int) durationMinutes;
        int seconds = (int) ((durationMinutes - minutes) * 60);
        tvDuration.setText(String.format("%d min %d sec", minutes, seconds));

        double estimatedPrice = distanceKm * PRICE_PER_KM;
        tvEstimatedPrice.setText(String.format("%.0f RSD", estimatedPrice));

        llEstimationResults.setVisibility(View.VISIBLE);
    }

    private void drawRouteOnMap(Road road, GeoPoint startPoint, GeoPoint endPoint) {
        if (routeOverlay != null) {
            mapView.getOverlays().remove(routeOverlay);
        }
        if (startMarker != null) {
            mapView.getOverlays().remove(startMarker);
        }
        if (endMarker != null) {
            mapView.getOverlays().remove(endMarker);
        }

        routeOverlay = RoadManager.buildRoadOverlay(road);
        routeOverlay.getOutlinePaint().setColor(Color.parseColor("#00a2ff"));
        routeOverlay.getOutlinePaint().setStrokeWidth(12f);
        mapView.getOverlays().add(routeOverlay);

        startMarker = new Marker(mapView);
        startMarker.setPosition(startPoint);
        startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        startMarker.setTitle(getString(R.string.start_location));
        Drawable startIcon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_start_marker);
        startMarker.setIcon(startIcon);
        mapView.getOverlays().add(startMarker);

        endMarker = new Marker(mapView);
        endMarker.setPosition(endPoint);
        endMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        endMarker.setTitle(getString(R.string.end_location));
        Drawable endIcon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_end_marker);
        endMarker.setIcon(endIcon);
        mapView.getOverlays().add(endMarker);

        mapView.zoomToBoundingBox(road.mBoundingBox, true);
        mapView.invalidate();
    }

    private void loadVehicles() {
        generateMockVehicles();
        updateVehicleCounts();
    }

    private void generateMockVehicles() {
        for (VehicleMarker vm : vehicleMarkers) {
            mapView.getOverlays().remove(vm.marker);
        }
        vehicleMarkers.clear();

        Random random = new Random();
        int numVehicles = 8 + random.nextInt(5); // 8-12 vehicles

        for (int i = 0; i < numVehicles; i++) {
            // Get random road location
            GeoPoint position = getRandomRoadLocation();
            boolean isAvailable = random.nextBoolean();

            VehicleMarker vehicleMarker = new VehicleMarker(
                    "Vehicle " + (i + 1),
                    position.getLatitude(),
                    position.getLongitude(),
                    isAvailable
            );

            Marker marker = new Marker(mapView);
            marker.setPosition(position);
            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
            marker.setTitle(vehicleMarker.name);
            marker.setSnippet(isAvailable ?
                    getString(R.string.status_available) :
                    getString(R.string.status_busy));

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
                simulateVehicleUpdates();
                updateHandler.postDelayed(this, 30000);
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

            // Simulate movement to nearby road location (only if available)
            if (vm.isAvailable && random.nextInt(3) == 0) { // 33% chance to move
                GeoPoint currentPos = vm.marker.getPosition();
                GeoPoint newPos = getNearbyRoadLocation(currentPos);

                vm.marker.setPosition(newPos);
                vm.latitude = newPos.getLatitude();
                vm.longitude = newPos.getLongitude();
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

        if (getActivity() != null) {
            AppCompatActivity activity = (AppCompatActivity) getActivity();
            if (activity.getSupportActionBar() != null) {
                activity.getSupportActionBar().setDisplayHomeAsUpEnabled(false);
                activity.getSupportActionBar().setTitle("Cuber");
            }
        }

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