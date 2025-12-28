package com.example.mobile.ui.fragments;

import android.app.DatePickerDialog;
import android.os.Bundle;
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
        loadMockData();
        setupRecyclerView();
        setupListeners();
    }

    private void initViews(View view) {
        etStartDate = view.findViewById(R.id.etEmail);
        etEndDate = view.findViewById(R.id.tvDate);
        btnFilter = view.findViewById(R.id.btnFilter);
        rvRides = view.findViewById(R.id.rvRides);

        // Make date fields non-editable (click to open calendar)
        etStartDate.setFocusable(false);
        etStartDate.setClickable(true);
        etEndDate.setFocusable(false);
        etEndDate.setClickable(true);
    }

    private void loadMockData() {
        allRides = new ArrayList<>();

        allRides.add(new Ride(
                1,
                "Narodnog fronta 23 → Dunavski park",
                "2024-12-15",
                "2024-12-15",
                45.50,
                "Completed",
                "09:30 AM",
                "11:15 AM",
                "1h 45min",
                "Marko Petrović",
                "+381 64 123 4567",
                89.5,
                "Credit Card",
                "Pleasant ride, passenger was on time."
        ));

        allRides.add(new Ride(
                2,
                "Bulevar Oslobođenja 30 → Trg Slobode",
                "2024-12-14",
                "2024-12-14",
                32.00,
                "Completed",
                "14:00 PM",
                "15:30 PM",
                "1h 30min",
                "Ana Jovanović",
                "+381 63 987 6543",
                72.3,
                "Cash",
                "Smooth journey, no issues."
        ));

        allRides.add(new Ride(
                3,
                "Železnička stanica Novi Sad → Limanski park",
                "2024-12-13",
                "2024-12-13",
                85.00,
                "Completed",
                "08:00 AM",
                "11:45 AM",
                "3h 45min",
                "Stefan Nikolić",
                "+381 65 555 1234",
                237.8,
                "Credit Card",
                "Long distance trip, passenger requested one rest stop."
        ));

        allRides.add(new Ride(
                4,
                "Spens (Bulevar cara Lazara) → Petrovaradinska tvrđava",
                "2024-12-12",
                "2024-12-12",
                38.50,
                "Cancelled",
                "16:00 PM",
                "17:30 PM",
                "1h 30min",
                "Jelena Đorđević",
                "+381 64 222 3333",
                115.2,
                "N/A",
                "Ride cancelled by passenger 30 minutes before scheduled time."
        ));

        allRides.add(new Ride(
                5,
                "Grbavica (Danila Kiša 18) → Spens",
                "2024-12-11",
                "2024-12-11",
                25.00,
                "Completed",
                "11:00 AM",
                "12:00 PM",
                "1h",
                "Milan Stojanović",
                "+381 66 777 8888",
                46.5,
                "Cash",
                null
        ));

        filteredRides = new ArrayList<>(allRides);
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

    private void showRideDetailsDialog(Ride ride) {
        RideDetailsDialogFragment dialog = RideDetailsDialogFragment.newInstance(ride);
        dialog.show(getParentFragmentManager(), "RideDetailsDialog");
    }
}