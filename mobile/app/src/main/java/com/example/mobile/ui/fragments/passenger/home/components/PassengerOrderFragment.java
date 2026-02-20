package com.example.mobile.ui.fragments.passenger.home.components;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mobile.R;
import com.example.mobile.dto.ride.FavoriteRoute;
import com.example.mobile.dto.ride.Location;
import com.example.mobile.dto.ride.OrderRequest;
import com.example.mobile.dto.ride.RidePreferences;
import com.example.mobile.dto.ride.RideRoute;
import com.example.mobile.dto.ride.Stop;
import com.example.mobile.enums.VehicleType;
import com.example.mobile.model.RideAssignmentResponse;
import com.example.mobile.network.RetrofitClient;
import com.example.mobile.ui.fragments.FavoriteRouteSelectorDialogFragment;
import com.example.mobile.ui.fragments.SchedulePickerDialogFragment;
import com.example.mobile.ui.fragments.passenger.home.PassengerViewModel;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PassengerOrderFragment extends Fragment implements SchedulePickerDialogFragment.OnTimeSelectedListener,
        FavoriteRouteSelectorDialogFragment.OnRouteSelectedListener{
    // ── Vehicle types (mirrors Angular environment.vehicleTypes) ──────────────
    private static final VehicleTypeOption[] VEHICLE_TYPES = {
            new VehicleTypeOption("standard", "🚗 Standard"),
            new VehicleTypeOption("luxury",   "✨ Luxury"),
            new VehicleTypeOption("van",      "🚐 Van")
    };

    // ── Views ─────────────────────────────────────────────────────────────────
    private TextInputEditText etPickupAddress;
    private TextInputEditText etDestinationAddress;
    private TextView tvPickupError;
    private TextView          tvDestinationError;
    private LinearLayout stopsContainer;
    private Spinner spinnerVehicleType;
    private MaterialCardView cardBabySeat;
    private MaterialCardView  cardPetFriendly;
    private MaterialCardView  cardSchedule;
    private TextView          tvScheduledTime;
    private LinearLayout      trackersContainer;
    private TextInputEditText etTrackerEmail;
    private Button btnAddTracker;

    // ── State ──────────────────────────────────────────────────────────────────
    private final List<View> stopViews  = new ArrayList<>();  // each stop's inflated view
    private final List<String> trackerEmails = new ArrayList<>();
    private boolean babySeatSelected  = false;
    private boolean petFriendly       = false;
    private Date scheduledTime     = null;

    // ─────────────────────────────────────────────────────────────────────────
    // Lifecycle
    // ─────────────────────────────────────────────────────────────────────────

    public PassengerOrderFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_passenger_order, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        bindViews(view);
        setupVehicleSpinner();
        setupToggleCards();
        setupTrackerInput();
        setupButtons(view);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // View binding
    // ─────────────────────────────────────────────────────────────────────────

    private void bindViews(View root) {
        etPickupAddress      = root.findViewById(R.id.etPickupAddress);
        etDestinationAddress = root.findViewById(R.id.etDestinationAddress);
        tvPickupError        = root.findViewById(R.id.tvPickupError);
        tvDestinationError   = root.findViewById(R.id.tvDestinationError);
        stopsContainer       = root.findViewById(R.id.stopsContainer);
        spinnerVehicleType   = root.findViewById(R.id.spinnerVehicleType);
        cardBabySeat         = root.findViewById(R.id.cardBabySeat);
        cardPetFriendly      = root.findViewById(R.id.cardPetFriendly);
        cardSchedule         = root.findViewById(R.id.cardSchedule);
        tvScheduledTime      = root.findViewById(R.id.tvScheduledTime);
        trackersContainer    = root.findViewById(R.id.trackersContainer);
        etTrackerEmail       = root.findViewById(R.id.etTrackerEmail);
        btnAddTracker        = root.findViewById(R.id.btnAddTracker);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Vehicle type spinner
    // ─────────────────────────────────────────────────────────────────────────

    private void setupVehicleSpinner() {
        String[] labels = new String[VEHICLE_TYPES.length];
        for (int i = 0; i < VEHICLE_TYPES.length; i++) {
            labels[i] = VEHICLE_TYPES[i].label;
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                labels);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerVehicleType.setAdapter(adapter);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Toggle cards (baby seat, pet friendly, schedule)
    // ─────────────────────────────────────────────────────────────────────────

    private void setupToggleCards() {
        cardBabySeat.setOnClickListener(v -> {
            babySeatSelected = !babySeatSelected;
            applyToggleStyle(cardBabySeat,
                    requireView().findViewById(R.id.ivBabySeatIcon),
                    requireView().findViewById(R.id.tvBabySeat),
                    babySeatSelected);
        });

        cardPetFriendly.setOnClickListener(v -> {
            petFriendly = !petFriendly;
            applyToggleStyle(cardPetFriendly,
                    requireView().findViewById(R.id.ivPetIcon),
                    requireView().findViewById(R.id.tvPetFriendly),
                    petFriendly);
        });

        cardSchedule.setOnClickListener(v -> {
            if (scheduledTime != null) {
                // Toggle OFF — clear the scheduled time
                scheduledTime = null;
                tvScheduledTime.setVisibility(View.GONE);
                applyScheduleCardStyle(false);
            } else {
                // Open the picker
                openSchedulePicker();
            }
        });
    }

    /** Applies active/inactive border+tint to a toggle card. */
    private void applyToggleStyle(MaterialCardView card,
                                  View icon,
                                  TextView label,
                                  boolean active) {
        int colorStroke = active
                ? getResources().getColor(R.color.base_600, null)
                : getResources().getColor(R.color.gray_100, null);
        int colorContent = active
                ? getResources().getColor(R.color.base_600, null)
                : getResources().getColor(R.color.black, null);

        card.setStrokeColor(colorStroke);
        label.setTextColor(colorContent);
        if (icon instanceof android.widget.ImageView) {
            ((android.widget.ImageView) icon)
                    .setColorFilter(colorContent, android.graphics.PorterDuff.Mode.SRC_IN);
        }
    }

    private void applyScheduleCardStyle(boolean active) {
        int colorStroke = active
                ? getResources().getColor(R.color.base_600, null)
                : getResources().getColor(R.color.gray_100, null);
        int colorContent = active
                ? getResources().getColor(R.color.base_600, null)
                : getResources().getColor(R.color.black, null);

        cardSchedule.setStrokeColor(colorStroke);

        TextView tvSchedule = requireView().findViewById(R.id.tvSchedule);
        tvSchedule.setTextColor(colorContent);

        android.widget.ImageView ivSchedule = requireView().findViewById(R.id.ivScheduleIcon);
        ivSchedule.setColorFilter(colorContent, android.graphics.PorterDuff.Mode.SRC_IN);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Stops management
    // ─────────────────────────────────────────────────────────────────────────

    private void addStop(@Nullable String prefilledAddress) {
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        View stopView = inflater.inflate(R.layout.item_stop, stopsContainer, false);

        TextView tvLabel = stopView.findViewById(R.id.tvStopLabel);
        tvLabel.setText("Stop " + (stopViews.size() + 1));

        TextInputEditText etStop = stopView.findViewById(R.id.etStopAddress);
        if (!TextUtils.isEmpty(prefilledAddress)) {
            etStop.setText(prefilledAddress);
        }

        Button btnRemove = stopView.findViewById(R.id.btnRemoveStop);
        btnRemove.setOnClickListener(v -> removeStop(stopView));

        stopViews.add(stopView);
        stopsContainer.addView(stopView);
        reindexStopLabels();
    }

    private void removeStop(View stopView) {
        stopViews.remove(stopView);
        stopsContainer.removeView(stopView);
        reindexStopLabels();
    }

    /** Re-labels all stops after an add/remove so they stay in order. */
    private void reindexStopLabels() {
        for (int i = 0; i < stopViews.size(); i++) {
            TextView lbl = stopViews.get(i).findViewById(R.id.tvStopLabel);
            lbl.setText("Stop " + (i + 1));
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Tracker emails
    // ─────────────────────────────────────────────────────────────────────────

    private void setupTrackerInput() {
        // Enable Add button only when email input is non-empty & valid
        etTrackerEmail.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                String email = s.toString().trim();
                boolean valid = !email.isEmpty() && isValidEmail(email);
                btnAddTracker.setEnabled(valid);
            }
        });

        etTrackerEmail.setOnEditorActionListener((v, actionId, event) -> {
            addTracker();
            return true;
        });

        btnAddTracker.setEnabled(false);
        btnAddTracker.setOnClickListener(v -> addTracker());
    }

    private void addTracker() {
        String email = etTrackerEmail.getText() != null
                ? etTrackerEmail.getText().toString().trim()
                : "";

        if (email.isEmpty() || !isValidEmail(email)) return;
        if (trackerEmails.contains(email)) {
            Toast.makeText(requireContext(), "Email already added", Toast.LENGTH_SHORT).show();
            return;
        }

        trackerEmails.add(email);
        inflateTrackerItem(email);
        etTrackerEmail.setText("");
    }

    private void inflateTrackerItem(String email) {
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        View item = inflater.inflate(R.layout.item_tracker, trackersContainer, false);

        TextView tvEmail = item.findViewById(R.id.tvTrackerEmail);
        tvEmail.setText(email);

        item.findViewById(R.id.btnRemoveTracker).setOnClickListener(v -> {
            trackerEmails.remove(email);
            trackersContainer.removeView(item);
        });

        trackersContainer.addView(item);
    }

    private boolean isValidEmail(String email) {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Dialogs
    // ─────────────────────────────────────────────────────────────────────────

    private void openSchedulePicker() {
        SchedulePickerDialogFragment dialog = new SchedulePickerDialogFragment();
        dialog.setListener(this);
        dialog.show(getChildFragmentManager(), SchedulePickerDialogFragment.TAG);
    }

    private void openFavoriteRoutes() {
        FavoriteRouteSelectorDialogFragment dialog = new FavoriteRouteSelectorDialogFragment();
        dialog.setListener(this);
        dialog.show(getChildFragmentManager(), FavoriteRouteSelectorDialogFragment.TAG);
    }

    // ── SchedulePickerDialog.OnTimeSelectedListener ───────────────────────────

    @Override
    public void onTimeSelected(Date date) {
        scheduledTime = date;

        // Format display label (mirrors Angular isTomorrow() + DatePipe:'HH:mm')
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault());
        java.util.Calendar cal  = java.util.Calendar.getInstance();
        java.util.Calendar calPicked = java.util.Calendar.getInstance();
        calPicked.setTime(date);

        boolean isTomorrow = calPicked.get(java.util.Calendar.DAY_OF_YEAR)
                != cal.get(java.util.Calendar.DAY_OF_YEAR);

        String label = (isTomorrow ? "Tomorrow, " : "Today, ") + sdf.format(date);
        tvScheduledTime.setText(label);
        tvScheduledTime.setVisibility(View.VISIBLE);
        applyScheduleCardStyle(true);
    }

    // ── FavoriteRouteSelectorDialog.OnRouteSelectedListener ───────────────────

    @Override
    public void onRouteSelected(FavoriteRoute favoriteRoute) {
        // Populate pickup
        if (favoriteRoute.getRoute().getPickupStop() != null) {
            etPickupAddress.setText(favoriteRoute.getRoute().getPickupStop().getAddress());
        }
        // Populate destination
        if (favoriteRoute.getRoute().getDestinationStop() != null) {
            etDestinationAddress.setText(favoriteRoute.getRoute().getDestinationStop().getAddress());
        }
        // Populate stops
        stopsContainer.removeAllViews();
        stopViews.clear();
        if (favoriteRoute.getRoute().getStops() != null) {
            for (Stop stop : favoriteRoute.getRoute().getStops()) {
                addStop(stop.getAddress());
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Buttons
    // ─────────────────────────────────────────────────────────────────────────

    private void setupButtons(View root) {
        root.findViewById(R.id.btnFavoriteRoutes).setOnClickListener(v -> openFavoriteRoutes());
        root.findViewById(R.id.btnAddStop).setOnClickListener(v -> addStop(null));
        root.findViewById(R.id.btnPreviewRide).setOnClickListener(v -> previewRoute());
        root.findViewById(R.id.btnBookRide).setOnClickListener(v -> bookRide());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Validation
    // ─────────────────────────────────────────────────────────────────────────

    /** Returns true if the route form is valid. Shows error messages otherwise. */
    private boolean validateRoute() {
        boolean valid = true;

        String pickup = etPickupAddress.getText() != null
                ? etPickupAddress.getText().toString().trim() : "";
        if (pickup.isEmpty()) {
            tvPickupError.setVisibility(View.VISIBLE);
            valid = false;
        } else {
            tvPickupError.setVisibility(View.GONE);
        }

        String destination = etDestinationAddress.getText() != null
                ? etDestinationAddress.getText().toString().trim() : "";
        if (destination.isEmpty()) {
            tvDestinationError.setVisibility(View.VISIBLE);
            valid = false;
        } else {
            tvDestinationError.setVisibility(View.GONE);
        }

        return valid;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Build request objects  (mirrors Angular getRoute() / getPreferences())
    // ─────────────────────────────────────────────────────────────────────────

    private RideRoute buildRoute() {
        String pickupAddress = etPickupAddress.getText() != null
                ? etPickupAddress.getText().toString().trim() : "";
        String destAddress = etDestinationAddress.getText() != null
                ? etDestinationAddress.getText().toString().trim() : "";

        // Location will be null / zero until you integrate geocoding.
        // The structure exactly mirrors the Angular getRoute() output.
        Stop pickup = new Stop(pickupAddress, new Location(null, null));
        Stop destination = new Stop(destAddress, new Location(null, null));

        List<Stop> stops = new ArrayList<>();
        for (View stopView : stopViews) {
            TextInputEditText etStop = stopView.findViewById(R.id.etStopAddress);
            String addr = etStop.getText() != null
                    ? etStop.getText().toString().trim() : "";
            if (!addr.isEmpty()) {
                stops.add(new Stop(addr, new Location(null, null)));
            }
        }

        return new RideRoute(pickup, destination, stops);
    }

    private RidePreferences buildPreferences() {
        int selectedPos = spinnerVehicleType.getSelectedItemPosition();
        VehicleType vehicleType = VehicleType.valueOf(
                VEHICLE_TYPES[selectedPos].id.toUpperCase());

        LocalDateTime scheduledFor = null;
        if (scheduledTime != null) {
            scheduledFor = LocalDateTime.ofInstant(
                    scheduledTime.toInstant(),
                    java.time.ZoneId.systemDefault());
        }

        return new RidePreferences(vehicleType, petFriendly, babySeatSelected, scheduledFor);
    }

    private String[] buildTrackers() {
        return trackerEmails.toArray(new String[0]);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Actions
    // ─────────────────────────────────────────────────────────────────────────

    /** Mirrors Angular showRoute() — calculates and draws the route on the map. */
    private void previewRoute() {
        if (!validateRoute()) return;

        RideRoute route = buildRoute();

        List<String> addresses = new ArrayList<>();
        addresses.add(route.getPickup().getAddress());
        for (Stop stop : route.getStops()) {
            addresses.add(stop.getAddress());
        }
        addresses.add(route.getDestination().getAddress());

        // TODO: Call your EstimationService / MapService equivalent here.
        // Example:
        // estimationService.calculateRouteFromAddresses(addresses, result -> {
        //     if (result != null) mapService.drawRoute(result.getRouteGeometry());
        // });

        Toast.makeText(requireContext(),
                "Preview: " + String.join(" → ", addresses),
                Toast.LENGTH_SHORT).show();
    }

    private void bookRide() {
        if (!validateRoute()) return;

        RideRoute route = buildRoute();
        RidePreferences preferences = buildPreferences();
        String[] trackers = buildTrackers();

        LocalDateTime createdAt = LocalDateTime.now();

        OrderRequest request = new OrderRequest(route, preferences, createdAt, trackers);

        RetrofitClient.getRideService(requireContext()).bookRide(request).enqueue(new Callback<Long>() {
            @Override
            public void onResponse(@NonNull Call<Long> call, @NonNull Response<Long> response) {
                if (response.isSuccessful() && response.body() != null) {
                    long rideId = response.body();
                    // Navigate to the ride-wait screen
                    navigateToRideWait(rideId);
                } else {
                    String msg = "Došlo je do greške pri naručivanju.";
                    Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Long> call, @NonNull Throwable t) {
                Toast.makeText(requireContext(),
                        "Network error: " + t.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void navigateToRideWait(long rideId) {
        // Replace with your Navigation Component / Intent call:
        // Bundle args = new Bundle();
        // args.putLong("rideId", rideId);
        // Navigation.findNavController(requireView()).navigate(R.id.action_to_rideWait, args);
        Toast.makeText(requireContext(), "Ride booked! ID: " + rideId, Toast.LENGTH_SHORT).show();
        RetrofitClient.getRideService(requireContext())
                .getRideAssignment(rideId)
                .enqueue(new Callback<RideAssignmentResponse>() {
                    @Override
                    public void onResponse(Call<RideAssignmentResponse> call, Response<RideAssignmentResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            // Ovo pokreće WebSocket i mijenja state na WAITING
                            PassengerViewModel vm = new ViewModelProvider(requireActivity())
                                    .get(PassengerViewModel.class);
                            vm.updateActiveRide(response.body());
                        }
                    }
                    @Override
                    public void onFailure(Call<RideAssignmentResponse> call, Throwable t) {
                        Toast.makeText(requireContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Internal helper model
    // ─────────────────────────────────────────────────────────────────────────

    private static class VehicleTypeOption {
        final String id;
        final String label;

        VehicleTypeOption(String id, String label) {
            this.id    = id;
            this.label = label;
        }
    }
}