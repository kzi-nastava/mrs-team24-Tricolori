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
        llAuthButtons.setVisibility(View.VISIBLE);

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

        mapContainer.addView(mapView);

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
        try {
            String query = address + ", Novi Sad, Serbia";
            String urlString = "https://nominatim.openstreetmap.org/search?q=" +
                    java.net.URLEncoder.encode(query, "UTF-8") +
                    "&format=json&limit=1";

            java.net.URL url = new java.net.URL(urlString);
            java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("User-Agent", "CuberApp/1.0");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);

            int responseCode = conn.getResponseCode();
            if (responseCode == 200) {
                java.io.BufferedReader reader = new java.io.BufferedReader(
                        new java.io.InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                String jsonResponse = response.toString();
                if (jsonResponse.startsWith("[") && jsonResponse.length() > 2) {
                    int latIndex = jsonResponse.indexOf("\"lat\":\"") + 7;
                    int latEnd = jsonResponse.indexOf("\"", latIndex);
                    int lonIndex = jsonResponse.indexOf("\"lon\":\"") + 7;
                    int lonEnd = jsonResponse.indexOf("\"", lonIndex);

                    if (latIndex > 7 && lonIndex > 7) {
                        String latStr = jsonResponse.substring(latIndex, latEnd);
                        String lonStr = jsonResponse.substring(lonIndex, lonEnd);
                        double lat = Double.parseDouble(latStr);
                        double lon = Double.parseDouble(lonStr);
                        Log.d(TAG, "Geocoded: " + address + " -> " + lat + ", " + lon);
                        return new GeoPoint(lat, lon);
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Geocoding error for: " + address, e);
        }

        Random random = new Random(address.hashCode());
        double lat = DEFAULT_LAT + (random.nextDouble() - 0.5) * 0.02;
        double lon = DEFAULT_LON + (random.nextDouble() - 0.5) * 0.02;
        Log.d(TAG, "Using fallback location for: " + address);
        return new GeoPoint(lat, lon);
    }

    private void snapToRoadAsync(GeoPoint point, RoadSnapCallback callback) {
        new Thread(() -> {
            try {
                RoadManager roadManager = new OSRMRoadManager(requireContext(),
                        Configuration.getInstance().getUserAgentValue());

                ArrayList<GeoPoint> waypoints = new ArrayList<>();
                waypoints.add(point);
                waypoints.add(new GeoPoint(point.getLatitude() + 0.0001, point.getLongitude() + 0.0001));

                Road road = roadManager.getRoad(waypoints);

                if (road != null && road.mStatus == Road.STATUS_OK && road.mRouteHigh != null && !road.mRouteHigh.isEmpty()) {
                    GeoPoint snappedPoint = road.mRouteHigh.get(0);
                    requireActivity().runOnUiThread(() -> callback.onSnapped(snappedPoint));
                } else {
                    requireActivity().runOnUiThread(() -> callback.onSnapped(point));
                }
            } catch (Exception e) {
                Log.e(TAG, "Error snapping to road", e);
                requireActivity().runOnUiThread(() -> callback.onSnapped(point));
            }
        }).start();
    }

    interface RoadSnapCallback {
        void onSnapped(GeoPoint snappedPoint);
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
        if (startIcon != null) {
            startIcon.setTint(ContextCompat.getColor(requireContext(), R.color.light_700));
        }
        startMarker.setIcon(startIcon);
        mapView.getOverlays().add(startMarker);

        endMarker = new Marker(mapView);
        endMarker.setPosition(endPoint);
        endMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        endMarker.setTitle(getString(R.string.end_location));
        Drawable endIcon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_end_marker);
        if (endIcon != null) {
            endIcon.setTint(Color.parseColor("#8B0000"));
        }
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
        int numVehicles = 8 + random.nextInt(5);

        for (int i = 0; i < numVehicles; i++) {
            double lat = DEFAULT_LAT + (random.nextDouble() - 0.5) * 0.04;
            double lon = DEFAULT_LON + (random.nextDouble() - 0.5) * 0.04;
            GeoPoint position = new GeoPoint(lat, lon);

            boolean isAvailable = random.nextBoolean();

            snapToRoadAsync(position, snappedPoint -> {
                VehicleMarker vehicleMarker = new VehicleMarker(
                        "Vehicle " + vehicleMarkers.size(),
                        snappedPoint.getLatitude(),
                        snappedPoint.getLongitude(),
                        isAvailable
                );

                if (mapView == null) {
                    return;
                }

                Marker marker = new Marker(mapView);
                marker.setPosition(snappedPoint);
                marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                marker.setTitle(vehicleMarker.name);
                marker.setSnippet(isAvailable ?
                        getString(R.string.status_available) :
                        getString(R.string.status_busy));

                Drawable icon = ContextCompat.getDrawable(requireContext(),
                        isAvailable ? R.drawable.ic_vehicle_available : R.drawable.ic_vehicle_busy);

                if (icon != null) {
                    if (isAvailable) {
                        icon.setTint(ContextCompat.getColor(requireContext(), R.color.light_700));
                    } else {
                        icon.setTint(Color.parseColor("#8B0000"));
                    }
                }
                marker.setIcon(icon);

                vehicleMarker.marker = marker;
                vehicleMarkers.add(vehicleMarker);
                mapView.getOverlays().add(marker);

                updateVehicleCounts();
                mapView.invalidate();
            });
        }
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
            if (random.nextInt(10) == 0) {
                vm.isAvailable = !vm.isAvailable;
                vm.marker.setSnippet(vm.isAvailable ?
                        getString(R.string.status_available) :
                        getString(R.string.status_busy));

                Drawable icon = ContextCompat.getDrawable(requireContext(),
                        vm.isAvailable ? R.drawable.ic_vehicle_available : R.drawable.ic_vehicle_busy);

                if (icon != null) {
                    if (vm.isAvailable) {
                        icon.setTint(ContextCompat.getColor(requireContext(), R.color.light_700));
                    } else {
                        icon.setTint(Color.parseColor("#8B0000"));
                    }
                }
                vm.marker.setIcon(icon);

                updateVehicleCounts();
            }

            if (vm.isAvailable && random.nextInt(5) == 0) {
                GeoPoint currentPos = vm.marker.getPosition();
                double newLat = currentPos.getLatitude() + (random.nextDouble() - 0.5) * 0.003;
                double newLon = currentPos.getLongitude() + (random.nextDouble() - 0.5) * 0.003;
                GeoPoint newPos = new GeoPoint(newLat, newLon);

                snapToRoadAsync(newPos, snappedPoint -> {
                    vm.marker.setPosition(snappedPoint);
                    vm.latitude = snappedPoint.getLatitude();
                    vm.longitude = snappedPoint.getLongitude();
                    mapView.invalidate();
                });
            }
        }

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
            llAuthButtons.setVisibility(View.VISIBLE);
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