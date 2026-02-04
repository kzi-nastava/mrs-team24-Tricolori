package com.example.mobile.ui.fragments;

import android.app.DatePickerDialog;
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
import com.example.mobile.dto.PageResponse;
import com.example.mobile.dto.ride.DriverRideHistoryResponse;
import com.example.mobile.network.RetrofitClient;
import com.example.mobile.network.RideService;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import com.example.mobile.R;
import com.example.mobile.ui.adapters.RideHistoryAdapter;
import com.example.mobile.ui.models.Ride;
import com.google.android.material.textfield.TextInputEditText;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class RideHistoryFragment extends Fragment {

    private TextInputEditText etStartDate, etEndDate;
    private Button btnFilter;
    private RecyclerView rvRides;
    private RideHistoryAdapter adapter;

    private List<Ride> allRides;
    private List<Ride> filteredRides;

    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    public RideHistoryFragment() {
        // Required empty constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the XML layout instead of creating programmatically
        return inflater.inflate(R.layout.fragment_ride_history, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        setupRecyclerView();
        setupListeners();
        fetchRideHistory();
    }

    private void initViews(View view) {
        etStartDate = view.findViewById(R.id.etStartDate);
        etEndDate = view.findViewById(R.id.etEndDate);
        btnFilter = view.findViewById(R.id.btnFilter);
        rvRides = view.findViewById(R.id.rvRides);

        // Make date fields non-editable (click to open calendar)
        etStartDate.setFocusable(false);
        etStartDate.setClickable(true);
        etEndDate.setFocusable(false);
        etEndDate.setClickable(true);
    }

    private void fetchRideHistory() {

        RideService rideService =
                RetrofitClient.getClient().create(RideService.class);

        rideService.getDriverRideHistory(0, 20)
                .enqueue(new Callback<PageResponse<DriverRideHistoryResponse>>() {

                    @Override
                    public void onResponse(
                            Call<PageResponse<DriverRideHistoryResponse>> call,
                            Response<PageResponse<DriverRideHistoryResponse>> response) {

                        Log.d("RideHistory", "Response code: " + response.code());

                        if (!response.isSuccessful() || response.body() == null) {
                            String errorBody = "";
                            try {
                                if (response.errorBody() != null) {
                                    errorBody = response.errorBody().string();
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            Log.e("RideHistory", "Error: " + response.code() + " - " + errorBody);
                            Toast.makeText(getContext(),
                                    "Error loading rides: " + response.code(),
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }

                        mapDtosToRides(response.body().getContent());
                    }

                    @Override
                    public void onFailure(
                            Call<PageResponse<DriverRideHistoryResponse>> call,
                            Throwable t) {

                        Toast.makeText(getContext(),
                                "Network error",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }


    private void setupRecyclerView() {
        adapter = new RideHistoryAdapter(filteredRides, ride -> {
            showRideDetailsDialog(ride);
        });

        rvRides.setLayoutManager(new LinearLayoutManager(getContext()));
        rvRides.setAdapter(adapter);
    }

    private void setupListeners() {
        etStartDate.setOnClickListener(v -> showDatePicker(true));
        etEndDate.setOnClickListener(v -> showDatePicker(false));
        btnFilter.setOnClickListener(v -> filterRides());
    }

    private void showDatePicker(boolean isStartDate) {
        Calendar calendar = Calendar.getInstance();

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                requireContext(),
                (view, year, month, dayOfMonth) -> {
                    calendar.set(year, month, dayOfMonth);
                    String selectedDate = dateFormat.format(calendar.getTime());

                    if (isStartDate) {
                        etStartDate.setText(selectedDate);
                    } else {
                        etEndDate.setText(selectedDate);
                    }
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );

        datePickerDialog.show();
    }

    private void filterRides() {
        String startDateStr = etStartDate.getText().toString();
        String endDateStr = etEndDate.getText().toString();

        if (startDateStr.isEmpty() && endDateStr.isEmpty()) {
            filteredRides = new ArrayList<>(allRides);
            adapter.updateData(filteredRides);
            return;
        }

        filteredRides = new ArrayList<>();

        try {
            Date startDate = startDateStr.isEmpty() ? null : dateFormat.parse(startDateStr);
            Date endDate = endDateStr.isEmpty() ? null : dateFormat.parse(endDateStr);

            for (Ride ride : allRides) {
                Date rideDate = dateFormat.parse(ride.getStartDate());

                if (rideDate != null) {
                    boolean include = true;

                    if (startDate != null && rideDate.before(startDate)) {
                        include = false;
                    }

                    if (endDate != null && rideDate.after(endDate)) {
                        include = false;
                    }

                    if (include) {
                        filteredRides.add(ride);
                    }
                }
            }

            adapter.updateData(filteredRides);

            if (filteredRides.isEmpty()) {
                Toast.makeText(getContext(), "No rides found in selected date range", Toast.LENGTH_SHORT).show();
            }

        } catch (ParseException e) {
            Toast.makeText(getContext(), "Error parsing dates", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    private void mapDtosToRides(List<DriverRideHistoryResponse> dtos) {

        allRides = new ArrayList<>();

        for (DriverRideHistoryResponse dto : dtos) {

            Ride ride = new Ride(
                    dto.getId().intValue(),
                    dto.getPickupAddress() + " → " + dto.getDestinationAddress(),

                    dto.getStartDate().substring(0,10),
                    dto.getEndDate() != null ? dto.getEndDate().substring(0,10) : "",

                    dto.getPrice() != null ? dto.getPrice() : 0.0,
                    dto.getStatus(),

                    "", "", "",  // vremena/duration trenutno nemamo

                    "", "", // passenger name/phone nemamo u history DTO

                    dto.getDistance() != null ? dto.getDistance() : 0.0,

                    "",  // payment method
                    null // note
            );

            allRides.add(ride);
        }

        filteredRides = new ArrayList<>(allRides);
        adapter.updateData(filteredRides);
    }


    private void showRideDetailsDialog(Ride ride) {
        RideDetailsDialogFragment dialog =
                RideDetailsDialogFragment.newInstance((long) ride.getId());

        dialog.show(getParentFragmentManager(), "RideDetailsDialog");
    }

}