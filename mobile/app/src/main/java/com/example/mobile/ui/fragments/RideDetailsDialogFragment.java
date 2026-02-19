package com.example.mobile.ui.fragments;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.navigation.NavController;

import com.example.mobile.R;
import com.example.mobile.dto.ride.DriverRideDetailResponse;
import com.example.mobile.dto.ride.PassengerRideDetailResponse;
import com.example.mobile.dto.ride.RideRatingStatusResponse;
import com.example.mobile.network.RetrofitClient;
import com.example.mobile.network.service.RideService;
import com.google.android.material.button.MaterialButton;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import retrofit2.Call;
import retrofit2.Callback;

public class RideDetailsDialogFragment extends DialogFragment {

    private static final String ARG_RIDE_ID = "ride_id";
    private static final String ARG_ROLE    = "role";
    private static final String TAG         = "RideDetailsDialog";

    public static RideDetailsDialogFragment newInstance(Long rideId) {
        return newInstance(rideId, DriverRideHistoryFragment.ROLE_DRIVER);
    }

    public static RideDetailsDialogFragment newInstance(Long rideId, String role) {
        RideDetailsDialogFragment f = new RideDetailsDialogFragment();
        Bundle args = new Bundle();
        args.putLong(ARG_RIDE_ID, rideId);
        args.putString(ARG_ROLE, role);
        f.setArguments(args);
        return f;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_ride_details_dialog, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Configuration.getInstance().setUserAgentValue(requireContext().getPackageName());

        long   rideId = getArguments().getLong(ARG_RIDE_ID);
        String role   = getArguments().getString(ARG_ROLE, DriverRideHistoryFragment.ROLE_DRIVER);

        if (DriverRideHistoryFragment.ROLE_PASSENGER.equals(role)) {
            fetchPassengerRideDetails(rideId, view);
        } else if ("ADMIN".equals(role)) {
            fetchAdminRideDetails(rideId, view);
        } else {
            fetchDriverRideDetails(rideId, view);
        }

        view.findViewById(R.id.btnClose).setOnClickListener(v -> dismiss());
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() != null && getDialog().getWindow() != null) {
            DisplayMetrics dm = new DisplayMetrics();
            getDialog().getWindow().getWindowManager().getDefaultDisplay().getMetrics(dm);
            getDialog().getWindow().setLayout(
                    (int) (dm.widthPixels * 0.9),
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
        }
    }

    private void fetchAdminRideDetails(long rideId, View view) {
        RetrofitClient.getClient(requireContext()).create(RideService.class)
                .getAdminRideDetail(rideId)
                .enqueue(new Callback<>() {
                    @Override
                    public void onResponse(Call<DriverRideDetailResponse> call,
                                           retrofit2.Response<DriverRideDetailResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            bindDriverData(view, response.body());
                            view.findViewById(R.id.btnTrackRide).setVisibility(View.GONE);
                        } else {
                            Log.e(TAG, "Admin detail error: " + response.code());
                        }
                    }

                    @Override
                    public void onFailure(Call<DriverRideDetailResponse> call, Throwable t) {
                        Log.e(TAG, "FAILURE", t);
                    }
                });
    }

    private void fetchDriverRideDetails(long rideId, View view) {
        RetrofitClient.getClient(requireContext()).create(RideService.class)
                .getDriverRideDetail(rideId)
                .enqueue(new Callback<DriverRideDetailResponse>() {
                    @Override
                    public void onResponse(Call<DriverRideDetailResponse> call,
                                           retrofit2.Response<DriverRideDetailResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            bindDriverData(view, response.body());
                        } else {
                            Log.e(TAG, "Driver detail error: " + response.code());
                        }
                    }

                    @Override
                    public void onFailure(Call<DriverRideDetailResponse> call, Throwable t) {
                        Log.e(TAG, "FAILURE", t);
                    }
                });
    }

    private void fetchPassengerRideDetails(long rideId, View view) {
        RetrofitClient.getClient(requireContext()).create(RideService.class)
                .getPassengerRideDetail(rideId)
                .enqueue(new Callback<PassengerRideDetailResponse>() {
                    @Override
                    public void onResponse(Call<PassengerRideDetailResponse> call,
                                           retrofit2.Response<PassengerRideDetailResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            bindPassengerData(view, response.body());
                            checkRatingStatus(view, rideId, response.body());
                        } else {
                            Log.e(TAG, "Passenger detail error: " + response.code());
                        }
                    }

                    @Override
                    public void onFailure(Call<PassengerRideDetailResponse> call, Throwable t) {
                        Log.e(TAG, "FAILURE", t);
                    }
                });
    }

    private void checkRatingStatus(View view, long rideId, PassengerRideDetailResponse dto) {
        MaterialButton btnRate = view.findViewById(R.id.btnRateRide);
        btnRate.setVisibility(View.GONE);

        RetrofitClient.getClient(requireContext()).create(RideService.class)
                .getRatingStatus(rideId)
                .enqueue(new Callback<RideRatingStatusResponse>() {
                    @Override
                    public void onResponse(Call<RideRatingStatusResponse> call,
                                           retrofit2.Response<RideRatingStatusResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            boolean canRate = Boolean.TRUE.equals(response.body().getCanRate());
                            boolean rated   = Boolean.TRUE.equals(response.body().getRated());
                            if (canRate && !rated) {
                                btnRate.setVisibility(View.VISIBLE);
                                btnRate.setOnClickListener(v ->
                                        openRatingScreen(dto.getId(), safe(dto.getDriverName())));
                            }
                        } else {
                            Log.w(TAG, "Rating status failed: " + response.code());
                        }
                    }

                    @Override
                    public void onFailure(Call<RideRatingStatusResponse> call, Throwable t) {
                        Log.w(TAG, "Rating status error", t);
                    }
                });
    }

    @SuppressLint("SetTextI18n")
    private void bindDriverData(View view, DriverRideDetailResponse dto) {
        setRoute(view, safe(dto.getPickupAddress()), safe(dto.getDropoffAddress()));

        String start = dto.getStartedAt() != null ? dto.getStartedAt() : dto.getCreatedAt();
        setTimes(view, start, dto.getCompletedAt());
        setDurationDistance(view, dto.getDuration(), dto.getDistance());
        setPrice(view, dto.getTotalPrice());
        setStatus(view, dto.getStatus());
        setPersonRow(view, "Passenger", safe(dto.getPassengerName()), safe(dto.getPassengerPhone()));
        bindRatings(view, "Passenger Rating", dto.getDriverRating(), dto.getVehicleRating(), dto.getRatingComment());

        view.findViewById(R.id.btnRateRide).setVisibility(View.GONE);
        view.findViewById(R.id.vehicleContainer).setVisibility(View.GONE);

        setupDriverTrackRideButton(view, dto);

        setupMap(view,
                dto.getPickupLatitude(),  dto.getPickupLongitude(),
                dto.getDropoffLatitude(), dto.getDropoffLongitude(),
                safe(dto.getPickupAddress()),
                safe(dto.getDropoffAddress()));
    }

    @SuppressLint("SetTextI18n")
    private void bindPassengerData(View view, PassengerRideDetailResponse dto) {
        setRoute(view, safe(dto.getPickupAddress()), safe(dto.getDropoffAddress()));

        String start = dto.getStartedAt() != null ? dto.getStartedAt() : dto.getCreatedAt();
        setTimes(view, start, dto.getCompletedAt());
        setDurationDistance(view, dto.getDuration(), dto.getDistance());
        setPrice(view, dto.getTotalPrice());
        setStatus(view, dto.getStatus());
        setPersonRow(view, "Driver", safe(dto.getDriverName()), safe(dto.getDriverPhone()));

        View vehicleContainer = view.findViewById(R.id.vehicleContainer);
        boolean hasVehicle =
                (dto.getVehicleModel() != null && !dto.getVehicleModel().isEmpty()) ||
                        (dto.getVehiclePlate() != null && !dto.getVehiclePlate().isEmpty());

        if (hasVehicle) {
            ((TextView) view.findViewById(R.id.tvDetailVehicleModel)).setText(safe(dto.getVehicleModel()));
            ((TextView) view.findViewById(R.id.tvDetailVehiclePlate)).setText(safe(dto.getVehiclePlate()));
            vehicleContainer.setVisibility(View.VISIBLE);
        } else {
            vehicleContainer.setVisibility(View.GONE);
        }

        bindRatings(view, "Your Rating", dto.getDriverRating(), dto.getVehicleRating(), dto.getRatingComment());
        view.findViewById(R.id.btnRateRide).setVisibility(View.GONE);

        setupTrackRideButton(view, dto);

        setupMap(view,
                dto.getPickupLatitude(),  dto.getPickupLongitude(),
                dto.getDropoffLatitude(), dto.getDropoffLongitude(),
                safe(dto.getPickupAddress()),
                safe(dto.getDropoffAddress()));
    }

    private void setupDriverTrackRideButton(View view, DriverRideDetailResponse dto) {
        MaterialButton btnTrack = view.findViewById(R.id.btnTrackRide);

        if ("ONGOING".equalsIgnoreCase(dto.getStatus())) {
            btnTrack.setVisibility(View.VISIBLE);
            btnTrack.setOnClickListener(v -> {
                dismiss();
                Bundle args = new Bundle();
                args.putLong("ride_id", dto.getId());

                NavController navController =
                        androidx.navigation.Navigation.findNavController(
                                requireActivity(), R.id.nav_host_fragment);

                navController.navigate(R.id.driverRideTrackingFragment, args);
            });
        } else {
            btnTrack.setVisibility(View.GONE);
        }
    }

    private void setRoute(View view, String pickup, String dropoff) {
        ((TextView) view.findViewById(R.id.tvDetailRoute)).setText(pickup + " → " + dropoff);
    }

    private void setTimes(View view, String start, String end) {
        ((TextView) view.findViewById(R.id.tvDetailStartTime)).setText(formatDateTime(start));
        ((TextView) view.findViewById(R.id.tvDetailEndTime)).setText(formatDateTime(end));
    }

    private void setDurationDistance(View view, Integer duration, Double distance) {
        ((TextView) view.findViewById(R.id.tvDetailDuration))
                .setText(duration != null ? duration + " min" : "-");
        ((TextView) view.findViewById(R.id.tvDetailDistance))
                .setText(distance != null
                        ? String.format(Locale.getDefault(), "%.1f km", distance)
                        : "-");
    }

    private void setPrice(View view, Double price) {
        ((TextView) view.findViewById(R.id.tvDetailPrice))
                .setText(price != null
                        ? String.format(Locale.getDefault(), "%.2f RSD", price)
                        : "-");
    }

    private void setStatus(View view, String status) {
        ((TextView) view.findViewById(R.id.tvDetailStatus)).setText(safe(status));
    }

    private void setPersonRow(View view, String label, String name, String phone) {
        ((TextView) view.findViewById(R.id.tvDetailPersonLabel)).setText(label);
        ((TextView) view.findViewById(R.id.tvDetailPersonName)).setText(name);
        ((TextView) view.findViewById(R.id.tvDetailPersonPhone)).setText(phone);
    }

    private void bindRatings(View view, String sectionLabel,
                             Integer driverRating, Integer vehicleRating, String comment) {
        View ratingContainer = view.findViewById(R.id.ratingContainer);

        if (driverRating != null || vehicleRating != null) {
            ((TextView) view.findViewById(R.id.tvRatingSectionLabel)).setText(sectionLabel);

            if (driverRating != null)
                ((TextView) view.findViewById(R.id.tvDriverRating))
                        .setText("Driver: " + stars(driverRating));

            if (vehicleRating != null)
                ((TextView) view.findViewById(R.id.tvVehicleRating))
                        .setText("Vehicle: " + stars(vehicleRating));

            ratingContainer.setVisibility(View.VISIBLE);
        } else {
            ratingContainer.setVisibility(View.GONE);
        }

        View notesContainer = view.findViewById(R.id.notesContainer);
        if (comment != null && !comment.isEmpty()) {
            ((TextView) view.findViewById(R.id.tvDetailNotes)).setText(comment);
            notesContainer.setVisibility(View.VISIBLE);
        } else {
            notesContainer.setVisibility(View.GONE);
        }
    }

    private void openRatingScreen(Long rideId, String driverName) {
        dismiss();

        NavController navController = androidx.navigation.Navigation.findNavController(
                requireActivity(), R.id.nav_host_fragment);

        Bundle args = new Bundle();
        args.putLong("ride_id", rideId);
        args.putString("driver_name", driverName);

        navController.navigate(R.id.action_to_rideRatingFragment, args);
    }

    private void setupTrackRideButton(View view, PassengerRideDetailResponse dto) {
        MaterialButton btnTrack = view.findViewById(R.id.btnTrackRide);

        if ("ONGOING".equalsIgnoreCase(dto.getStatus())) {
            btnTrack.setVisibility(View.VISIBLE);
            btnTrack.setOnClickListener(v -> {
                dismiss();
                Bundle args = new Bundle();
                args.putLong("ride_id", dto.getId());

                NavController navController = androidx.navigation.Navigation.findNavController(
                        requireActivity(), R.id.nav_host_fragment);
                navController.navigate(R.id.passengerRideTrackingFragment, args);
            });
        } else {
            btnTrack.setVisibility(View.GONE);
        }
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private void setupMap(View view,
                          Double pickupLat,  Double pickupLng,
                          Double dropoffLat, Double dropoffLng,
                          String pickupAddr, String dropoffAddr) {
        MapView map = view.findViewById(R.id.rideMap);

        if (pickupLat == null || dropoffLat == null) {
            map.setVisibility(View.GONE);
            Log.w(TAG, "No coordinates available for map");
            return;
        }

        map.setVisibility(View.VISIBLE);
        map.setMultiTouchControls(true);

        GeoPoint start = new GeoPoint(pickupLat, pickupLng);
        GeoPoint end   = new GeoPoint(dropoffLat, dropoffLng);

        Marker mStart = new Marker(map);
        mStart.setPosition(start);
        mStart.setTitle("Pickup");
        mStart.setSnippet(pickupAddr);
        mStart.setIcon(getResources().getDrawable(R.drawable.ic_marker_start));
        mStart.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        map.getOverlays().add(mStart);

        Marker mEnd = new Marker(map);
        mEnd.setPosition(end);
        mEnd.setTitle("Dropoff");
        mEnd.setSnippet(dropoffAddr);
        mEnd.setIcon(getResources().getDrawable(R.drawable.ic_marker_end));
        mEnd.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        map.getOverlays().add(mEnd);

        IMapController controller = map.getController();
        controller.setZoom(13.0);
        controller.setCenter(start);

        drawRoute(map, start, end);
    }

    private void drawRoute(MapView map, GeoPoint start, GeoPoint end) {
        new Thread(() -> {
            try {
                String url = "https://router.project-osrm.org/route/v1/driving/"
                        + start.getLongitude() + "," + start.getLatitude()
                        + ";" + end.getLongitude() + "," + end.getLatitude()
                        + "?overview=full&geometries=geojson";

                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder().url(url).build();
                Response response = client.newCall(request).execute();

                if (!response.isSuccessful()) {
                    drawFallbackLine(map, start, end);
                    return;
                }

                JSONObject json = new JSONObject(response.body().string());

                if (!json.has("routes") || json.getJSONArray("routes").length() == 0) {
                    drawFallbackLine(map, start, end);
                    return;
                }

                JSONArray coords = json.getJSONArray("routes")
                        .getJSONObject(0)
                        .getJSONObject("geometry")
                        .getJSONArray("coordinates");

                List<GeoPoint> pts = new ArrayList<>();
                for (int i = 0; i < coords.length(); i++) {
                    JSONArray c = coords.getJSONArray(i);
                    pts.add(new GeoPoint(c.getDouble(1), c.getDouble(0)));
                }

                requireActivity().runOnUiThread(() -> {
                    Polyline line = new Polyline();
                    line.setPoints(pts);
                    line.setColor(getResources().getColor(R.color.base_800));
                    line.setWidth(8f);
                    map.getOverlays().add(0, line);
                    BoundingBox bb = BoundingBox.fromGeoPoints(pts);
                    map.post(() -> map.zoomToBoundingBox(bb, true, 100));
                    map.invalidate();
                    Log.d(TAG, "Route drawn with " + pts.size() + " points");
                });

            } catch (Exception e) {
                Log.e(TAG, "Error drawing route", e);
                drawFallbackLine(map, start, end);
            }
        }).start();
    }

    private void drawFallbackLine(MapView map, GeoPoint start, GeoPoint end) {
        requireActivity().runOnUiThread(() -> {
            List<GeoPoint> pts = new ArrayList<>();
            pts.add(start);
            pts.add(end);

            Polyline line = new Polyline();
            line.setPoints(pts);
            line.setColor(getResources().getColor(R.color.base_800));
            line.setWidth(8f);
            line.getOutlinePaint().setStrokeCap(android.graphics.Paint.Cap.ROUND);
            map.getOverlays().add(0, line);

            BoundingBox bb = new BoundingBox(
                    Math.max(start.getLatitude(), end.getLatitude()),
                    Math.max(start.getLongitude(), end.getLongitude()),
                    Math.min(start.getLatitude(), end.getLatitude()),
                    Math.min(start.getLongitude(), end.getLongitude()));
            map.post(() -> map.zoomToBoundingBox(bb, true, 100));
            map.invalidate();
            Log.d(TAG, "Fallback line drawn");
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        MapView map = getView() != null ? getView().findViewById(R.id.rideMap) : null;
        if (map != null) map.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        MapView map = getView() != null ? getView().findViewById(R.id.rideMap) : null;
        if (map != null) map.onPause();
    }

    private String safe(String s) { return s == null ? "-" : s; }

    private String formatDateTime(String iso) {
        if (iso == null || iso.length() < 16) return "-";
        return iso.substring(0, 10) + " " + iso.substring(11, 16);
    }

    private String stars(int r) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 5; i++) sb.append(i < r ? "★" : "☆");
        return sb.toString();
    }
}
