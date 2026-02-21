package com.example.mobile.ui.fragments.history;

import android.app.DatePickerDialog;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.mobile.R;
import com.example.mobile.ui.fragments.RideDetailsDialogFragment;
import com.example.mobile.ui.models.Ride;
import com.example.mobile.utils.ShakeDetector;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class RideHistoryFragment extends Fragment {

    private static final String ARG_ROLE = "role";

    // UI Components - Filters
    private TextInputEditText etStartDate, etEndDate, etPersonEmail;
    private TextInputLayout tilPersonEmail;
    private Button btnApplyFilter, btnClearFilter;

    // UI Components - List
    private RecyclerView rvRides;
    private ProgressBar progressBar;
    private TextView tvEmptyState;
    private SwipeRefreshLayout swipeRefresh;

    // UI Components - Pagination
    private Button btnPrevPage, btnNextPage;
    private TextView tvPageInfo, tvRideCount;

    // UI Components - Sorting headers (optional)
    private View sortRouteHeader, sortDateHeader, sortPriceHeader, sortStatusHeader;

    // ViewModel
    private RideHistoryViewModel viewModel;

    // Adapter
    private PagedRideHistoryAdapter adapter;

    private SensorManager sensorManager;
    private ShakeDetector shakeDetector;

    private static final String[][] SORT_CYCLE = {
            {"createdAt", "DESC"},
            {"createdAt", "ASC"},
            {"price",     "DESC"},
            {"price",     "ASC"},
            {"status",    "ASC"},
            {"route",     "ASC"},
    };
    private int sortCycleIndex = 0;

    private final SimpleDateFormat dateFormat =
            new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    // Factory methods
    public static RideHistoryFragment forPassenger() {
        return newInstance(RideHistoryViewModel.Role.PASSENGER);
    }

    public static RideHistoryFragment forAdmin() {
        return newInstance(RideHistoryViewModel.Role.ADMIN);
    }

    private static RideHistoryFragment newInstance(RideHistoryViewModel.Role role) {
        RideHistoryFragment f = new RideHistoryFragment();
        Bundle args = new Bundle();
        args.putString(ARG_ROLE, role.name());
        f.setArguments(args);
        return f;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_ride_history, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize ViewModel
        viewModel = new ViewModelProvider(this).get(RideHistoryViewModel.class);

        // Set role from arguments
        String roleString = getArguments() != null
                ? getArguments().getString(ARG_ROLE, RideHistoryViewModel.Role.PASSENGER.name())
                : RideHistoryViewModel.Role.PASSENGER.name();

        RideHistoryViewModel.Role role =
                RideHistoryViewModel.Role.valueOf(roleString);
        viewModel.setRole(role);

        initViews(view, role);
        setupRecyclerView();
        setupListeners();
        observeViewModel();

        setupShakeDetector();

        // Initial load
        viewModel.loadRideHistory(requireContext());
    }

    @Override
    public void onResume() {
        super.onResume();
        Sensor accel = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        if (accel != null) {
            sensorManager.registerListener(shakeDetector, accel,
                    SensorManager.SENSOR_DELAY_UI);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        sensorManager.unregisterListener(shakeDetector);
    }

    private void initViews(View view, RideHistoryViewModel.Role role) {
        // Filter inputs
        etStartDate = view.findViewById(R.id.etStartDate);
        etEndDate = view.findViewById(R.id.etEndDate);
        etPersonEmail = view.findViewById(R.id.etPersonEmail);
        tilPersonEmail = view.findViewById(R.id.tilPersonEmail);
        btnApplyFilter = view.findViewById(R.id.btnApplyFilter);
        btnClearFilter = view.findViewById(R.id.btnClearFilter);

        // Show/hide email filter for admin only
        if (role == RideHistoryViewModel.Role.ADMIN) {
            tilPersonEmail.setVisibility(View.VISIBLE);
        } else {
            tilPersonEmail.setVisibility(View.GONE);
        }

        // RecyclerView
        rvRides = view.findViewById(R.id.rvRides);
        progressBar = view.findViewById(R.id.progressBar);
        tvEmptyState = view.findViewById(R.id.tvEmptyState);
        swipeRefresh = view.findViewById(R.id.swipeRefresh);

        // Pagination
        btnPrevPage = view.findViewById(R.id.btnPrevPage);
        btnNextPage = view.findViewById(R.id.btnNextPage);
        tvPageInfo = view.findViewById(R.id.tvPageInfo);
        tvRideCount = view.findViewById(R.id.tvRideCount);

        // Sorting headers
        sortRouteHeader = view.findViewById(R.id.sortRouteHeader);
        sortDateHeader = view.findViewById(R.id.sortDateHeader);
        sortPriceHeader = view.findViewById(R.id.sortPriceHeader);
        sortStatusHeader = view.findViewById(R.id.sortStatusHeader);

        // Make date fields clickable
        etStartDate.setFocusable(false);
        etStartDate.setClickable(true);
        etEndDate.setFocusable(false);
        etEndDate.setClickable(true);
    }

    private void setupRecyclerView() {
        adapter = new PagedRideHistoryAdapter(new ArrayList<>(), this::onRideTapped);

        rvRides.setLayoutManager(new LinearLayoutManager(getContext()));
        rvRides.setAdapter(adapter);
    }

    private void setupListeners() {
        // Date pickers
        etStartDate.setOnClickListener(v -> showDatePicker(true));
        etEndDate.setOnClickListener(v -> showDatePicker(false));

        // Filter buttons
        btnApplyFilter.setOnClickListener(v -> onFilterChange());
        btnClearFilter.setOnClickListener(v -> clearFilters());

        // Pagination
        btnPrevPage.setOnClickListener(v -> {
            viewModel.previousPage();
            viewModel.loadRideHistory(requireContext());
        });

        btnNextPage.setOnClickListener(v -> {
            viewModel.nextPage();
            viewModel.loadRideHistory(requireContext());
        });

        // Swipe to refresh
        swipeRefresh.setOnRefreshListener(() -> {
            viewModel.resetPage();
            viewModel.loadRideHistory(requireContext());
        });

        // Sorting headers
        sortRouteHeader.setOnClickListener(v -> toggleSort("route"));
        sortDateHeader.setOnClickListener(v -> toggleSort("createdAt"));
        sortPriceHeader.setOnClickListener(v -> toggleSort("price"));
        sortStatusHeader.setOnClickListener(v -> toggleSort("status"));
    }

    private void toggleSort(String column) {
        String currentSort = viewModel.getSortBy();
        String currentDirection = viewModel.getSortDirection();

        if (currentSort.equals(column)) {
            // Toggle direction
            String newDirection = currentDirection.equals("DESC") ? "ASC" : "DESC";
            viewModel.setSorting(column, newDirection);
        } else {
            // New column, default to DESC
            viewModel.setSorting(column, "DESC");
        }

        viewModel.loadRideHistory(requireContext());
    }

    private void observeViewModel() {
        // Observe rides
        viewModel.getRides().observe(getViewLifecycleOwner(), rides -> {
            adapter.updateData(rides);

            // Show/hide empty state
            if (rides.isEmpty()) {
                rvRides.setVisibility(View.GONE);
                tvEmptyState.setVisibility(View.VISIBLE);
            } else {
                rvRides.setVisibility(View.VISIBLE);
                tvEmptyState.setVisibility(View.GONE);
            }
        });

        // Observe loading state
        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            if (swipeRefresh.isRefreshing()) {
                swipeRefresh.setRefreshing(false);
            }

            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            rvRides.setVisibility(isLoading ? View.GONE : View.VISIBLE);

            // Disable buttons during loading
            btnApplyFilter.setEnabled(!isLoading);
            btnClearFilter.setEnabled(!isLoading);

            updatePaginationUI();
        });

        // Observe errors
        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
            }
        });

        // Observe pagination changes
        viewModel.getTotalElements().observe(getViewLifecycleOwner(), total -> {
            updatePaginationUI();
        });

        viewModel.getCurrentPage().observe(getViewLifecycleOwner(), page -> {
            updatePaginationUI();
        });
    }

    private void updatePaginationUI() {
        Boolean isLoading = viewModel.getIsLoading().getValue();
        boolean loading = isLoading != null && isLoading;

        Integer currentPage = viewModel.getCurrentPage().getValue();
        int pageNum = currentPage != null ? currentPage : 0;

        // Update page info
        tvPageInfo.setText(String.format(Locale.getDefault(),
                "Page %d", pageNum + 1));

        // Update ride count
        Integer total = viewModel.getTotalElements().getValue();
        int totalCount = total != null ? total : 0;
        int currentCount = adapter.getItemCount();

        tvRideCount.setText(String.format(Locale.getDefault(),
                "Showing %d of %d rides", currentCount, totalCount));

        // Enable/disable pagination buttons
        btnPrevPage.setEnabled(!loading && viewModel.canGoPrevious());
        btnNextPage.setEnabled(!loading && viewModel.canGoNext());
    }

    private void onFilterChange() {
        String startDate = etStartDate.getText() != null
                ? etStartDate.getText().toString() : "";
        String endDate = etEndDate.getText() != null
                ? etEndDate.getText().toString() : "";
        String email = etPersonEmail.getText() != null
                ? etPersonEmail.getText().toString() : "";

        viewModel.setDateRange(startDate, endDate);
        viewModel.setPersonEmail(email);
        viewModel.resetPage();
        viewModel.loadRideHistory(requireContext());
    }

    private void clearFilters() {
        etStartDate.setText("");
        etEndDate.setText("");
        etPersonEmail.setText("");
        viewModel.clearFilters();
        viewModel.loadRideHistory(requireContext());
    }

    private void showDatePicker(boolean isStartDate) {
        Calendar cal = Calendar.getInstance();
        new DatePickerDialog(
                requireContext(),
                (v, year, month, day) -> {
                    cal.set(year, month, day);
                    String selected = dateFormat.format(cal.getTime());
                    if (isStartDate) {
                        etStartDate.setText(selected);
                    } else {
                        etEndDate.setText(selected);
                    }
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
        ).show();
    }

    private void onRideTapped(Ride ride) {
        String roleString = getArguments() != null
                ? getArguments().getString(ARG_ROLE, RideHistoryViewModel.Role.PASSENGER.name())
                : RideHistoryViewModel.Role.PASSENGER.name();

        RideDetailsDialogFragment dialog =
                RideDetailsDialogFragment.newInstance((long) ride.getId(), roleString);
        dialog.show(getParentFragmentManager(), "RideDetailsDialog");
    }

    private void setupShakeDetector() {
        sensorManager = (SensorManager) requireContext()
                .getSystemService(Context.SENSOR_SERVICE);

        shakeDetector = new ShakeDetector();
        shakeDetector.setOnShakeListener(count -> {
            sortCycleIndex = (sortCycleIndex + 1) % SORT_CYCLE.length;
            String col = SORT_CYCLE[sortCycleIndex][0];
            String dir = SORT_CYCLE[sortCycleIndex][1];

            viewModel.setSorting(col, dir);
            viewModel.loadRideHistory(requireContext());

            String msg = "Sort: " + col + " " + dir;
            Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
        });
    }
}