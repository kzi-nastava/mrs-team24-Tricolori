package com.example.mobile.ui.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.mobile.R;


import android.app.DatePickerDialog;
import android.graphics.Color;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.mobile.dto.report.DailyStatisticDTO;
import com.example.mobile.dto.report.ReportResponse;
import com.example.mobile.network.RetrofitClient;
import com.example.mobile.network.service.ReportService;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.Chart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserReportsFragment extends Fragment {

    // ─── Argument keys ────────────────────────────────────────────────────────
    private static final String ARG_IS_ADMIN = "is_admin";

    // ─── Scope values ─────────────────────────────────────────────────────────
    private static final String SCOPE_ALL        = "ALL";
    private static final String SCOPE_INDIVIDUAL = "INDIVIDUAL";

    // ─── Date format sent to backend ──────────────────────────────────────────
    private static final String BACKEND_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";
    private static final String DISPLAY_DATE_FORMAT = "dd.MM.yyyy";

    // ─── State ────────────────────────────────────────────────────────────────
    private boolean isAdmin;
    private String  currentScope = SCOPE_ALL;
    private Calendar dateFrom;
    private Calendar dateTo;

    // ─── Views ────────────────────────────────────────────────────────────────
    private LinearLayout layoutScopeToggle;
    private TextView     btnScopeAll;
    private TextView     btnScopeIndividual;
    private LinearLayout layoutEmailField;
    private TextView     etUserEmail;       // using TextInputEditText but cast as TextView is fine for getValue
    private TextView     tvDateFrom;
    private TextView     tvDateTo;
    private Button       btnGenerateReport;

    private ProgressBar  progressBar;
    private TextView     tvErrorMessage;
    private TextView     tvEmptyState;
    private LinearLayout layoutResults;

    private TextView tvTotalRides;
    private TextView tvAvgRides;
    private TextView tvTotalKm;
    private TextView tvAvgKm;
    private TextView tvTotalMoney;
    private TextView tvAvgMoney;

    private BarChart  chartRidesPerDay;
    private LineChart chartKmPerDay;
    private BarChart  chartMoneyPerDay;


    // ─── Lifecycle ────────────────────────────────────────────────────────────

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_user_reports, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        SharedPreferences prefs = requireActivity().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        String role = prefs.getString("user_role", "");
        if ("ROLE_ADMIN".equals(role))
            isAdmin = true;

        bindViews(view);
        configureForRole();
        setupListeners();
        setupCharts();
    }

    // ─── Binding & setup ─────────────────────────────────────────────────────

    private void bindViews(View v) {
        layoutScopeToggle  = v.findViewById(R.id.layoutScopeToggle);
        btnScopeAll        = v.findViewById(R.id.btnScopeAll);
        btnScopeIndividual = v.findViewById(R.id.btnScopeIndividual);
        layoutEmailField   = v.findViewById(R.id.layoutEmailField);
        etUserEmail        = v.findViewById(R.id.etUserEmail);
        tvDateFrom         = v.findViewById(R.id.tvDateFrom);
        tvDateTo           = v.findViewById(R.id.tvDateTo);
        btnGenerateReport  = v.findViewById(R.id.btnGenerateReport);

        progressBar        = v.findViewById(R.id.progressBar);
        tvErrorMessage     = v.findViewById(R.id.tvErrorMessage);
        tvEmptyState       = v.findViewById(R.id.tvEmptyState);
        layoutResults      = v.findViewById(R.id.layoutResults);

        tvTotalRides  = v.findViewById(R.id.tvTotalRides);
        tvAvgRides    = v.findViewById(R.id.tvAvgRides);
        tvTotalKm     = v.findViewById(R.id.tvTotalKm);
        tvAvgKm       = v.findViewById(R.id.tvAvgKm);
        tvTotalMoney  = v.findViewById(R.id.tvTotalMoney);
        tvAvgMoney    = v.findViewById(R.id.tvAvgMoney);

        chartRidesPerDay  = v.findViewById(R.id.chartRidesPerDay);
        chartKmPerDay     = v.findViewById(R.id.chartKmPerDay);
        chartMoneyPerDay  = v.findViewById(R.id.chartMoneyPerDay);
    }

    /** Show/hide admin-only UI elements */
    private void configureForRole() {
        if (isAdmin) {
            layoutScopeToggle.setVisibility(View.VISIBLE);
            updateScopeToggleUI();
        } else {
            layoutScopeToggle.setVisibility(View.GONE);
            layoutEmailField.setVisibility(View.GONE);
        }
    }

    private void setupListeners() {
        // Scope toggle (admin only)
        btnScopeAll.setOnClickListener(v -> setScope(SCOPE_ALL));
        btnScopeIndividual.setOnClickListener(v -> setScope(SCOPE_INDIVIDUAL));

        // Date pickers
        tvDateFrom.setOnClickListener(v -> showDatePicker(true));
        tvDateTo.setOnClickListener(v -> showDatePicker(false));

        // Generate
        btnGenerateReport.setOnClickListener(v -> generateReport());
    }

    /*--- Scope toggle ---*/

    private void setScope(String newScope) {
        currentScope = newScope;
        updateScopeToggleUI();

        // Show/hide email field
        layoutEmailField.setVisibility(
                SCOPE_INDIVIDUAL.equals(newScope) ? View.VISIBLE : View.GONE);

        // Clear previous results
        clearResults();
    }

    private void updateScopeToggleUI() {
        boolean allSelected = SCOPE_ALL.equals(currentScope);

        // Selected pill — white bg + deep text
        btnScopeAll.setBackgroundResource(
                allSelected ? R.drawable.bg_toggle_selected : android.R.color.transparent);
        btnScopeAll.setTextColor(
                allSelected ? getResources().getColor(R.color.deep_600, null)
                        : getResources().getColor(R.color.gray_500, null));

        btnScopeIndividual.setBackgroundResource(
                !allSelected ? R.drawable.bg_toggle_selected : android.R.color.transparent);
        btnScopeIndividual.setTextColor(
                !allSelected ? getResources().getColor(R.color.deep_600, null)
                        : getResources().getColor(R.color.gray_500, null));
    }

    // ─── Date picker ─────────────────────────────────────────────────────────

    private void showDatePicker(boolean isFrom) {
        Calendar initial = isFrom
                ? (dateFrom != null ? dateFrom : Calendar.getInstance())
                : (dateTo   != null ? dateTo   : Calendar.getInstance());

        DatePickerDialog dialog = new DatePickerDialog(
                requireContext(),
                (datePicker, year, month, dayOfMonth) -> {
                    Calendar selected = Calendar.getInstance();
                    selected.set(year, month, dayOfMonth, 0, 0, 0);
                    selected.set(Calendar.MILLISECOND, 0);

                    SimpleDateFormat displayFmt = new SimpleDateFormat(DISPLAY_DATE_FORMAT, Locale.getDefault());
                    if (isFrom) {
                        dateFrom = selected;
                        tvDateFrom.setText(displayFmt.format(selected.getTime()));
                    } else {
                        dateTo = selected;
                        tvDateTo.setText(displayFmt.format(selected.getTime()));
                    }
                },
                initial.get(Calendar.YEAR),
                initial.get(Calendar.MONTH),
                initial.get(Calendar.DAY_OF_MONTH)
        );
        dialog.show();
    }

    // ─── Generate report ─────────────────────────────────────────────────────

    private void generateReport() {
        if (!validateForm()) return;

        setLoadingState(true);
        clearResults();

        // Format dates for backend
        SimpleDateFormat backendFmt = new SimpleDateFormat(BACKEND_DATE_FORMAT, Locale.getDefault());
        String fromStr = backendFmt.format(dateFrom.getTime());

        Calendar toEnd = (Calendar) dateTo.clone();
        toEnd.set(Calendar.HOUR_OF_DAY, 23);
        toEnd.set(Calendar.MINUTE, 59);
        toEnd.set(Calendar.SECOND, 59);
        String toStr = backendFmt.format(toEnd.getTime());

        Call<ReportResponse> call;

        if (isAdmin) {
            String email = etUserEmail.getText() != null
                    ? etUserEmail.getText().toString().trim()
                    : "";
            call = RetrofitClient.getReportService(requireContext()).getAdminReport(fromStr, toStr, currentScope, email);
        } else {
            call = RetrofitClient.getReportService(requireContext()).getPersonalReport(fromStr, toStr);
        }

        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<ReportResponse> call,
                                   @NonNull Response<ReportResponse> response) {
                if (!isAdded()) return;
                setLoadingState(false);

                if (response.isSuccessful() && response.body() != null) {
                    populateResults(response.body());
                } else {
                    showError(isAdmin
                            ? "Error fetching comprehensive data."
                            : "Error fetching personal data.");
                }
            }

            @Override
            public void onFailure(@NonNull Call<ReportResponse> call, @NonNull Throwable t) {
                if (!isAdded()) return;
                setLoadingState(false);
                showError("Network error: " + t.getMessage());
            }
        });
    }

    /*--- Validation ---*/

    private boolean validateForm() {
        if (dateFrom == null || dateTo == null) {
            if (isAdmin && SCOPE_INDIVIDUAL.equals(currentScope)) {
                showError("Please select both dates and email.");
            } else {
                showError("Please select both dates.");
            }
            return false;
        }

        if (isAdmin && SCOPE_INDIVIDUAL.equals(currentScope)) {
            String email = etUserEmail.getText() != null
                    ? etUserEmail.getText().toString().trim()
                    : "";
            if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                showError("Please enter a valid user email.");
                return false;
            }
        }

        return true;
    }

    // ─── UI state helpers ────────────────────────────────────────────────────

    private void setLoadingState(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        btnGenerateReport.setEnabled(!loading);
        btnGenerateReport.setAlpha(loading ? 0.5f : 1.0f);
        if (loading) {
            tvEmptyState.setVisibility(View.GONE);
            tvErrorMessage.setVisibility(View.GONE);
        }
    }

    private void showError(String message) {
        tvErrorMessage.setText(message);
        tvErrorMessage.setVisibility(View.VISIBLE);
        tvEmptyState.setVisibility(View.GONE);
        layoutResults.setVisibility(View.GONE);
    }

    private void clearResults() {
        tvErrorMessage.setVisibility(View.GONE);
        layoutResults.setVisibility(View.GONE);
        tvEmptyState.setVisibility(View.VISIBLE);
    }

    // ─── Populate results ────────────────────────────────────────────────────

    private void populateResults(ReportResponse data) {
        tvEmptyState.setVisibility(View.GONE);
        tvErrorMessage.setVisibility(View.GONE);
        layoutResults.setVisibility(View.VISIBLE);

        // Stat cards
        tvTotalRides.setText(String.valueOf(data.getTotalCount()));
        tvAvgRides.setText(String.format(Locale.getDefault(), "Average: %.1f / dan", data.getAverageCount()));

        tvTotalKm.setText(String.format(Locale.getDefault(), "%.1f", data.getTotalDistance()));
        tvAvgKm.setText(String.format(Locale.getDefault(), "Average: %.1f / dan", data.getAverageDistance()));

        tvTotalMoney.setText(String.format(Locale.getDefault(), "%.0f", data.getTotalMoney()));
        tvAvgMoney.setText(String.format(Locale.getDefault(), "Average: %.0f RSD / dan", data.getAverageMoney()));

        // Charts
        List<DailyStatisticDTO> stats = data.getDailyStatistics();
        populateBarChart(chartRidesPerDay, stats, "count",    "#00cc92");
        populateLineChart(chartKmPerDay,   stats, "distance", "#00a2ff");
        populateBarChart(chartMoneyPerDay, stats, "money",    "#6366f1");
    }

    // ─── Chart setup ─────────────────────────────────────────────────────────

    /** Apply shared base config to both chart types */
    private void applyBaseChartConfig(Chart<?> chart,
                                      List<String> labels) {
        chart.getDescription().setEnabled(false);
        chart.getLegend().setEnabled(false);
        chart.setTouchEnabled(true);

        XAxis xAxis = null;
        if (chart instanceof BarChart) {
            xAxis = ((BarChart) chart).getXAxis();
        } else if (chart instanceof LineChart) {
            xAxis = ((LineChart) chart).getXAxis();
        }

        if (xAxis != null) {
            xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
            xAxis.setGranularity(1f);
            xAxis.setDrawGridLines(false);
            xAxis.setTextColor(Color.parseColor("#6b7280")); // gray_500
            xAxis.setTextSize(10f);
            xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
            xAxis.setLabelRotationAngle(-35f);
        }
    }

    private void setupCharts() {
        // Style left axis on all charts (called once, charts are empty at this point)
        styleLeftAxis(chartRidesPerDay.getAxisLeft());
        styleLeftAxis(chartKmPerDay.getAxisLeft());
        styleLeftAxis(chartMoneyPerDay.getAxisLeft());

        chartRidesPerDay.getAxisRight().setEnabled(false);
        chartKmPerDay.getAxisRight().setEnabled(false);
        chartMoneyPerDay.getAxisRight().setEnabled(false);
    }

    private void styleLeftAxis(com.github.mikephil.charting.components.YAxis axis) {
        axis.setTextColor(Color.parseColor("#6b7280"));
        axis.setTextSize(10f);
        axis.setDrawGridLines(true);
        axis.setGridColor(Color.parseColor("#e5e7eb")); // gray_200
        axis.setAxisMinimum(0f);
    }

    private void populateBarChart(BarChart chart,
                                  List<DailyStatisticDTO> stats,
                                  String field,
                                  String hexColor) {
        List<BarEntry>  entries = new ArrayList<>();
        List<String>    labels  = new ArrayList<>();

        for (int i = 0; i < stats.size(); i++) {
            DailyStatisticDTO s = stats.get(i);
            float value = getFieldValue(s, field);
            entries.add(new BarEntry(i, value));
            labels.add(s.getDate());
        }

        BarDataSet dataSet = new BarDataSet(entries, "");
        dataSet.setColor(Color.parseColor(hexColor));
        dataSet.setDrawValues(false);

        BarData barData = new BarData(dataSet);
        barData.setBarWidth(0.6f);

        applyBaseChartConfig(chart, labels);
        chart.setData(barData);
        chart.invalidate();
    }

    private void populateLineChart(LineChart chart,
                                   List<DailyStatisticDTO> stats,
                                   String field,
                                   String hexColor) {
        List<Entry>  entries = new ArrayList<>();
        List<String> labels  = new ArrayList<>();

        for (int i = 0; i < stats.size(); i++) {
            DailyStatisticDTO s = stats.get(i);
            entries.add(new Entry(i, getFieldValue(s, field)));
            labels.add(s.getDate());
        }

        LineDataSet dataSet = new LineDataSet(entries, "");
        int color = Color.parseColor(hexColor);
        dataSet.setColor(color);
        dataSet.setCircleColor(color);
        dataSet.setLineWidth(2f);
        dataSet.setCircleRadius(3f);
        dataSet.setDrawValues(false);
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        dataSet.setDrawFilled(true);
        dataSet.setFillColor(color);
        dataSet.setFillAlpha(25); // ~10% opacity, matching `rgba(0,162,255,0.1)`

        applyBaseChartConfig(chart, labels);
        chart.setData(new LineData(dataSet));
        chart.invalidate();
    }

    private float getFieldValue(DailyStatisticDTO s, String field) {
        switch (field) {
            case "count":    return s.getCount() != null    ? s.getCount().floatValue()    : 0f;
            case "distance": return s.getDistance() != null ? s.getDistance().floatValue() : 0f;
            case "money":    return s.getMoney() != null    ? s.getMoney().floatValue()     : 0f;
            default:         return 0f;
        }
    }
}