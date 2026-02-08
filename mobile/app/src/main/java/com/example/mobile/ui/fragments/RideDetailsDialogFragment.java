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

import com.example.mobile.R;
import com.example.mobile.dto.ride.DriverRideDetailResponse;
import com.example.mobile.network.RetrofitClient;
import com.example.mobile.network.service.RideService;

import org.osmdroid.config.Configuration;
import org.osmdroid.views.MapView;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;
import org.osmdroid.api.IMapController;

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
    private static final String TAG = "RideDetailsDialog";

    public static RideDetailsDialogFragment newInstance(Long rideId) {
        RideDetailsDialogFragment fragment = new RideDetailsDialogFragment();
        Bundle args = new Bundle();
        args.putLong(ARG_RIDE_ID, rideId);
        fragment.setArguments(args);
        return fragment;
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

        Configuration.getInstance()
                .setUserAgentValue(requireContext().getPackageName());

        long rideId = getArguments().getLong(ARG_RIDE_ID);

        fetchRideDetails(rideId, view);

        view.findViewById(R.id.btnClose)
                .setOnClickListener(v -> dismiss());
    }

    private void fetchRideDetails(long rideId, View view) {

        RideService rideService =
                RetrofitClient.getClient(requireContext())
                        .create(RideService.class);

        rideService.getDriverRideDetail(rideId)
                .enqueue(new Callback<DriverRideDetailResponse>() {

                    @Override
                    public void onResponse(
                            Call<DriverRideDetailResponse> call,
                            retrofit2.Response<DriverRideDetailResponse> response) {

                        if (response.isSuccessful() && response.body() != null) {

                            DriverRideDetailResponse dto = response.body();

                            Log.d(TAG,"createdAt=" + dto.getCreatedAt());
                            Log.d(TAG,"startedAt=" + dto.getStartedAt());
                            Log.d(TAG,"completedAt=" + dto.getCompletedAt());

                            bindData(view, dto);

                        } else {
                            Log.e(TAG,"Error code: " + response.code());
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<DriverRideDetailResponse> call,
                            Throwable t) {
                        Log.e(TAG,"FAILURE", t);
                    }
                });
    }

    @Override
    public void onStart() {
        super.onStart();

        if (getDialog() != null && getDialog().getWindow() != null) {
            DisplayMetrics dm = new DisplayMetrics();
            getDialog().getWindow().getWindowManager()
                    .getDefaultDisplay().getMetrics(dm);

            getDialog().getWindow().setLayout(
                    (int)(dm.widthPixels * 0.9),
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
        }
    }

    private String formatDateTime(String iso) {
        if (iso == null || iso.length() < 16) return "-";
        return iso.substring(0,10) + " " + iso.substring(11,16);
    }

    private void bindData(View view, DriverRideDetailResponse dto) {

        TextView tvRoute = view.findViewById(R.id.tvDetailRoute);
        TextView tvStartTime = view.findViewById(R.id.tvDetailStartTime);
        TextView tvEndTime = view.findViewById(R.id.tvDetailEndTime);
        TextView tvDuration = view.findViewById(R.id.tvDetailDuration);
        TextView tvDistance = view.findViewById(R.id.tvDetailDistance);
        TextView tvPrice = view.findViewById(R.id.tvDetailPrice);
        TextView tvStatus = view.findViewById(R.id.tvDetailStatus);
        TextView tvPassengerName = view.findViewById(R.id.tvDetailPassengerName);
        TextView tvPassengerPhone = view.findViewById(R.id.tvDetailPassengerPhone);
        TextView tvNotes = view.findViewById(R.id.tvDetailNotes);
        View notesContainer = view.findViewById(R.id.notesContainer);

        TextView tvDriverRating = view.findViewById(R.id.tvDriverRating);
        TextView tvVehicleRating = view.findViewById(R.id.tvVehicleRating);
        View ratingContainer = view.findViewById(R.id.ratingContainer);

        tvRoute.setText(
                safe(dto.getPickupAddress()) + " → " +
                        safe(dto.getDropoffAddress())
        );

        String start =
                dto.getStartedAt() != null
                        ? dto.getStartedAt()
                        : dto.getCreatedAt();

        tvStartTime.setText(formatDateTime(start));
        tvEndTime.setText(formatDateTime(dto.getCompletedAt()));

        tvDuration.setText(
                dto.getDuration() != null ? dto.getDuration()+" min" : "-"
        );

        tvDistance.setText(
                dto.getDistance()!=null ?
                        String.format(Locale.getDefault(),"%.1f km", dto.getDistance()) : "-"
        );

        tvPrice.setText(
                dto.getTotalPrice()!=null ?
                        String.format(Locale.getDefault(),"%.2f RSD", dto.getTotalPrice()) : "-"
        );

        tvStatus.setText(safe(dto.getStatus()));
        tvPassengerName.setText(safe(dto.getPassengerName()));
        tvPassengerPhone.setText(safe(dto.getPassengerPhone()));

        // ⭐ RATINGS
        if (dto.getDriverRating()!=null || dto.getVehicleRating()!=null) {

            if (dto.getDriverRating()!=null)
                tvDriverRating.setText("Driver: "+stars(dto.getDriverRating()));

            if (dto.getVehicleRating()!=null)
                tvVehicleRating.setText("Vehicle: "+stars(dto.getVehicleRating()));

            ratingContainer.setVisibility(View.VISIBLE);
        } else ratingContainer.setVisibility(View.GONE);

        // COMMENT
        if (dto.getRatingComment()!=null && !dto.getRatingComment().isEmpty()) {
            tvNotes.setText(dto.getRatingComment());
            notesContainer.setVisibility(View.VISIBLE);
        } else notesContainer.setVisibility(View.GONE);

        setupMap(view, dto);
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private void setupMap(View view, DriverRideDetailResponse dto) {

        MapView map = view.findViewById(R.id.rideMap);

        // Check if we have coordinates
        if (dto.getPickupLatitude()==null || dto.getDropoffLatitude()==null) {
            map.setVisibility(View.GONE);
            Log.w(TAG, "No coordinates available for map");
            return;
        }

        map.setVisibility(View.VISIBLE);
        map.setMultiTouchControls(true);

        GeoPoint start = new GeoPoint(
                dto.getPickupLatitude(),
                dto.getPickupLongitude()
        );

        GeoPoint end = new GeoPoint(
                dto.getDropoffLatitude(),
                dto.getDropoffLongitude()
        );

        // Add pickup marker
        Marker mStart = new Marker(map);
        mStart.setPosition(start);
        mStart.setTitle("Pickup");
        mStart.setSnippet(safe(dto.getPickupAddress()));
        mStart.setIcon(
                getResources().getDrawable(R.drawable.ic_marker_start)
        );
        mStart.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        map.getOverlays().add(mStart);

        // Add dropoff marker
        Marker mEnd = new Marker(map);
        mEnd.setPosition(end);
        mEnd.setTitle("Dropoff");
        mEnd.setSnippet(safe(dto.getDropoffAddress()));
        mEnd.setIcon(
                getResources().getDrawable(R.drawable.ic_marker_end)
        );
        mEnd.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        map.getOverlays().add(mEnd);

        // Initial map positioning (will be adjusted after route is drawn)
        IMapController controller = map.getController();
        controller.setZoom(13.0);
        controller.setCenter(start);

        // Draw route and adjust bounds
        drawRoute(map, start, end);
    }

    private void drawRoute(MapView map, GeoPoint start, GeoPoint end) {

        new Thread(() -> {
            try {

                String url =
                        "https://router.project-osrm.org/route/v1/driving/"
                                + start.getLongitude()+","+start.getLatitude()
                                + ";"
                                + end.getLongitude()+","+end.getLatitude()
                                + "?overview=full&geometries=geojson";

                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder().url(url).build();
                Response response = client.newCall(request).execute();

                if (!response.isSuccessful()) {
                    Log.e(TAG, "Route request failed: " + response.code());
                    // Draw straight line as fallback
                    drawFallbackLine(map, start, end);
                    return;
                }

                String responseBody = response.body().string();
                JSONObject json = new JSONObject(responseBody);

                if (!json.has("routes") || json.getJSONArray("routes").length() == 0) {
                    Log.e(TAG, "No routes in response");
                    drawFallbackLine(map, start, end);
                    return;
                }

                JSONArray coords =
                        json.getJSONArray("routes")
                                .getJSONObject(0)
                                .getJSONObject("geometry")
                                .getJSONArray("coordinates");

                List<GeoPoint> pts = new ArrayList<>();

                for(int i=0; i<coords.length(); i++){
                    JSONArray c = coords.getJSONArray(i);
                    // GeoJSON format: [longitude, latitude]
                    pts.add(new GeoPoint(c.getDouble(1), c.getDouble(0)));
                }

                requireActivity().runOnUiThread(() -> {
                    Polyline line = new Polyline();
                    line.setPoints(pts);
                    line.setColor(
                            getResources().getColor(R.color.base_800)
                    );
                    line.setWidth(8f);
                    map.getOverlays().add(0, line); // Add at index 0 so it's below markers

                    // Adjust map bounds to show entire route with padding
                    BoundingBox boundingBox = BoundingBox.fromGeoPoints(pts);
                    map.post(() -> map.zoomToBoundingBox(boundingBox, true, 100));

                    map.invalidate();
                    Log.d(TAG, "Route drawn successfully with " + pts.size() + " points");
                });

            } catch(Exception e){
                Log.e(TAG, "Error drawing route", e);
                // Draw fallback line on error
                drawFallbackLine(map, start, end);
            }
        }).start();
    }

    /**
     * Draw a simple straight line between start and end points as fallback
     */
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

            // Adjust bounds to show both points
            BoundingBox boundingBox = new BoundingBox(
                    Math.max(start.getLatitude(), end.getLatitude()),
                    Math.max(start.getLongitude(), end.getLongitude()),
                    Math.min(start.getLatitude(), end.getLatitude()),
                    Math.min(start.getLongitude(), end.getLongitude())
            );
            map.post(() -> map.zoomToBoundingBox(boundingBox, true, 100));

            map.invalidate();
            Log.d(TAG, "Fallback line drawn");
        });
    }

    private String safe(String s){
        return s==null ? "-" : s;
    }

    private String stars(int r){
        StringBuilder s=new StringBuilder();
        for(int i=0;i<5;i++)
            s.append(i<r?"★":"☆");
        return s.toString();
    }

    @Override
    public void onResume() {
        super.onResume();
        MapView map = getView() != null ? getView().findViewById(R.id.rideMap) : null;
        if (map != null) {
            map.onResume();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        MapView map = getView() != null ? getView().findViewById(R.id.rideMap) : null;
        if (map != null) {
            map.onPause();
        }
    }
}