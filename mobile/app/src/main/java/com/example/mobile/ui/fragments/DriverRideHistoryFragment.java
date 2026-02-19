package com.example.mobile.ui.fragments;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mobile.R;
import com.example.mobile.dto.ride.DriverRideHistoryResponse;
import com.example.mobile.dto.ride.PassengerRideHistoryResponse;
import com.example.mobile.network.RetrofitClient;
import com.example.mobile.network.service.RideService;
import com.example.mobile.ui.adapters.RideHistoryAdapter;
import com.example.mobile.ui.models.Ride;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DriverRideHistoryFragment extends Fragment {


    public static final String ROLE_DRIVER    = "DRIVER";
    public static final String ROLE_PASSENGER = "PASSENGER";

    private static final String ARG_ROLE = "role";
    private static final String TAG      = "RideHistoryFragment";


    public static DriverRideHistoryFragment forDriver() {
        return newInstance(ROLE_DRIVER);
    }

    public static DriverRideHistoryFragment forPassenger() {
        return newInstance(ROLE_PASSENGER);
    }

    private static DriverRideHistoryFragment newInstance(String role) {
        DriverRideHistoryFragment f = new DriverRideHistoryFragment();
        Bundle args = new Bundle();
        args.putString(ARG_ROLE, role);
        f.setArguments(args);
        return f;
    }


    private String role;

    private TextInputEditText etStartDate, etEndDate;
    private Button btnFilter;
    private RecyclerView rvRides;
    private RideHistoryAdapter adapter;

    private List<Ride> allRides      = new ArrayList<>();
    private List<Ride> filteredRides = new ArrayList<>();

    private final SimpleDateFormat dateFormat =
            new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    public DriverRideHistoryFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_driver_ride_history, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        role = getArguments() != null
                ? getArguments().getString(ARG_ROLE, ROLE_DRIVER)
                : ROLE_DRIVER;

        initViews(view);
        setupRecyclerView();
        setupListeners();
        fetchRideHistory();
    }


    private void initViews(View view) {
        etStartDate = view.findViewById(R.id.etStartDate);
        etEndDate   = view.findViewById(R.id.etEndDate);
        btnFilter   = view.findViewById(R.id.btnFilter);
        rvRides     = view.findViewById(R.id.rvRides);

        etStartDate.setFocusable(false);
        etStartDate.setClickable(true);
        etEndDate.setFocusable(false);
        etEndDate.setClickable(true);
    }

    private void setupRecyclerView() {
        adapter = new RideHistoryAdapter(filteredRides, this::onRideTapped);
        rvRides.setLayoutManager(new LinearLayoutManager(getContext()));
        rvRides.setAdapter(adapter);
    }

    private void setupListeners() {
        etStartDate.setOnClickListener(v -> showDatePicker(true));
        etEndDate.setOnClickListener(v -> showDatePicker(false));
        btnFilter.setOnClickListener(v -> filterRides());
    }


    private void fetchRideHistory() {
        SharedPreferences prefs =
                requireContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        String token = prefs.getString("jwt_token", null);

        if (token == null || token.isEmpty()) {
            Toast.makeText(getContext(), "Please log in first", Toast.LENGTH_SHORT).show();
            return;
        }

        if (isDriver()) {
            fetchDriverHistory();
        } else {
            fetchPassengerHistory();
        }
    }

    // Driver history — returns List<DriverRideHistoryResponse> directly
    private void fetchDriverHistory() {
        RideService rideService =
                RetrofitClient.getClient(requireContext()).create(RideService.class);

        rideService.getDriverRideHistory(null, null, "createdAt", "DESC")
                .enqueue(new Callback<List<DriverRideHistoryResponse>>() {

                    @Override
                    public void onResponse(Call<List<DriverRideHistoryResponse>> call,
                                           Response<List<DriverRideHistoryResponse>> response) {

                        Log.d(TAG, "Driver response code: " + response.code());

                        if (!response.isSuccessful() || response.body() == null) {
                            logError(response);
                            Toast.makeText(getContext(), "Error loading rides",
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }

                        Log.d(TAG, "Driver rides fetched: " + response.body().size());
                        mapDriverDtos(response.body());
                    }

                    @Override
                    public void onFailure(Call<List<DriverRideHistoryResponse>> call,
                                          Throwable t) {
                        Log.e(TAG, "Network failure: " + t.getMessage());
                        Toast.makeText(getContext(), "Network error", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // Passenger history — backend returns Page<PassengerRideHistoryResponse>,
    // so we parse the "content" array out of the JSON manually.
    private void fetchPassengerHistory() {
        RideService rideService =
                RetrofitClient.getClient(requireContext()).create(RideService.class);

        rideService.getPassengerRideHistory(null, null, 0, 1000, "createdAt,DESC")
                .enqueue(new Callback<ResponseBody>() {

                    @Override
                    public void onResponse(Call<ResponseBody> call,
                                           Response<ResponseBody> response) {

                        Log.d(TAG, "Passenger response code: " + response.code());

                        if (!response.isSuccessful() || response.body() == null) {
                            logError(response);
                            Toast.makeText(getContext(), "Error loading rides",
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }

                        try {
                            String json = response.body().string();
                            JSONArray content = new JSONObject(json).getJSONArray("content");

                            List<PassengerRideHistoryResponse> dtos = new ArrayList<>();
                            for (int i = 0; i < content.length(); i++) {
                                JSONObject o = content.getJSONObject(i);
                                PassengerRideHistoryResponse dto = new PassengerRideHistoryResponse();
                                dto.setId(o.optLong("id"));
                                dto.setPickupAddress(o.optString("pickupAddress", ""));
                                dto.setDestinationAddress(o.optString("destinationAddress", ""));
                                dto.setStartDate(o.optString("startDate", ""));
                                dto.setEndDate(o.optString("endDate", ""));
                                dto.setPrice(o.has("price") && !o.isNull("price")
                                        ? o.getDouble("price") : null);
                                dto.setStatus(o.optString("status", ""));
                                dto.setDistance(o.has("distance") && !o.isNull("distance")
                                        ? o.getDouble("distance") : null);
                                dto.setDriverName(o.optString("driverName", ""));
                                dto.setDriverPhone(o.optString("driverPhone", ""));
                                dto.setVehicleModel(o.optString("vehicleModel", ""));
                                dto.setVehiclePlate(o.optString("vehiclePlate", ""));
                                dto.setRated(o.optBoolean("rated", false));
                                dtos.add(dto);
                            }

                            Log.d(TAG, "Passenger rides fetched: " + dtos.size());
                            mapPassengerDtos(dtos);

                        } catch (Exception e) {
                            Log.e(TAG, "JSON parse error", e);
                            Toast.makeText(getContext(), "Error parsing rides",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        Log.e(TAG, "Network failure: " + t.getMessage());
                        Toast.makeText(getContext(), "Network error", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void mapDriverDtos(List<DriverRideHistoryResponse> dtos) {
        allRides = new ArrayList<>();

        for (DriverRideHistoryResponse dto : dtos) {
            allRides.add(new Ride(
                    dto.getId().intValue(),
                    dto.getPickupAddress() + " → " + dto.getDestinationAddress(),
                    safeDate(dto.getStartDate()),
                    safeDate(dto.getEndDate()),
                    dto.getPrice()    != null ? dto.getPrice()    : 0.0,
                    dto.getStatus(),
                    "", "", "",
                    "", "",
                    dto.getDistance() != null ? dto.getDistance() : 0.0,
                    "", null
            ));
        }

        updateList();
    }

    private void mapPassengerDtos(List<PassengerRideHistoryResponse> dtos) {
        allRides = new ArrayList<>();

        for (PassengerRideHistoryResponse dto : dtos) {
            allRides.add(new Ride(
                    dto.getId().intValue(),
                    dto.getPickupAddress() + " → " + dto.getDestinationAddress(),
                    safeDate(dto.getStartDate()),
                    safeDate(dto.getEndDate()),
                    dto.getPrice()    != null ? dto.getPrice()    : 0.0,
                    dto.getStatus(),
                    "", "", "",
                    "", "",
                    dto.getDistance() != null ? dto.getDistance() : 0.0,
                    "", null
            ));
        }

        updateList();
    }

    private void updateList() {
        filteredRides = new ArrayList<>(allRides);
        adapter.updateData(filteredRides);
    }

    private void filterRides() {
        String startStr = etStartDate.getText() != null
                ? etStartDate.getText().toString() : "";
        String endStr   = etEndDate.getText() != null
                ? etEndDate.getText().toString() : "";

        if (startStr.isEmpty() && endStr.isEmpty()) {
            filteredRides = new ArrayList<>(allRides);
            adapter.updateData(filteredRides);
            return;
        }

        filteredRides = new ArrayList<>();

        try {
            Date startDate = startStr.isEmpty() ? null : dateFormat.parse(startStr);
            Date endDate   = endStr.isEmpty()   ? null : dateFormat.parse(endStr);

            for (Ride ride : allRides) {
                if (ride.getStartDate() == null || ride.getStartDate().isEmpty()) continue;
                Date rideDate = dateFormat.parse(ride.getStartDate());
                if (rideDate == null) continue;

                boolean include = true;
                if (startDate != null && rideDate.before(startDate)) include = false;
                if (endDate   != null && rideDate.after(endDate))    include = false;
                if (include) filteredRides.add(ride);
            }

            adapter.updateData(filteredRides);

            if (filteredRides.isEmpty()) {
                Toast.makeText(getContext(),
                        "No rides found in selected date range",
                        Toast.LENGTH_SHORT).show();
            }

        } catch (ParseException e) {
            Toast.makeText(getContext(), "Error parsing dates", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    private void showDatePicker(boolean isStartDate) {
        Calendar cal = Calendar.getInstance();
        new DatePickerDialog(
                requireContext(),
                (v, year, month, day) -> {
                    cal.set(year, month, day);
                    String selected = dateFormat.format(cal.getTime());
                    if (isStartDate) etStartDate.setText(selected);
                    else             etEndDate.setText(selected);
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
        ).show();
    }


    private void onRideTapped(Ride ride) {
        RideDetailsDialogFragment dialog =
                RideDetailsDialogFragment.newInstance((long) ride.getId(), role);
        dialog.show(getParentFragmentManager(), "RideDetailsDialog");
    }


    private boolean isDriver() {
        return ROLE_DRIVER.equals(role);
    }

    private String safeDate(String date) {
        return date != null && date.length() >= 10 ? date.substring(0, 10) : "";
    }

    private void logError(Response<?> response) {
        try {
            String body = response.errorBody() != null
                    ? response.errorBody().string() : "";
            Log.e(TAG, "Error " + response.code() + ": " + body);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}