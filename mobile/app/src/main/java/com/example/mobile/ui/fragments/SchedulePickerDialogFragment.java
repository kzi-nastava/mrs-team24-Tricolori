package com.example.mobile.ui.fragments;

import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.TimePicker;

import com.example.mobile.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SchedulePickerDialogFragment extends DialogFragment {
    public SchedulePickerDialogFragment() {
        // Required empty public constructor
    }
    public static final String TAG = "SchedulePickerDialog";

    // ── Listener ──────────────────────────────────────────────────────────────

    public interface OnTimeSelectedListener {
        void onTimeSelected(Date date);
    }

    private OnTimeSelectedListener listener;

    public void setListener(OnTimeSelectedListener listener) {
        this.listener = listener;
    }

    // ── State ─────────────────────────────────────────────────────────────────

    private final List<Date> timeSlots    = new ArrayList<>();
    private Date             selectedTime = null;
    private boolean          isCustomMode = false;

    // ── Views ─────────────────────────────────────────────────────────────────

    private View       panelQuickSlots;
    private View       panelCustomTime;
    private GridView gridTimeSlots;
    private TimePicker timePicker;
    private Button btnToggleMode;
    private Button     btnConfirmTime;

    // ─────────────────────────────────────────────────────────────────────────

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_schedule_picker_dialog, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        panelQuickSlots = view.findViewById(R.id.panelQuickSlots);
        panelCustomTime = view.findViewById(R.id.panelCustomTime);
        gridTimeSlots   = view.findViewById(R.id.gridTimeSlots);
        timePicker      = view.findViewById(R.id.timePicker);
        btnToggleMode   = view.findViewById(R.id.btnToggleMode);
        btnConfirmTime  = view.findViewById(R.id.btnConfirmTime);

        generateTimeSlots();
        setupGrid();
        setupTimePicker();
        setupButtons();
    }

    @Override
    public void onStart() {
        super.onStart();
        // Make the dialog fill most of the screen width
        Dialog dialog = getDialog();
        if (dialog != null && dialog.getWindow() != null) {
            dialog.getWindow().setLayout(
                    (int) (getResources().getDisplayMetrics().widthPixels * 0.92),
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
    }

    // ── Time slot generation  (mirrors Angular generateTimeSlots()) ───────────

    private void generateTimeSlots() {
        timeSlots.clear();
        long now = System.currentTimeMillis();
        long quarterMs = 15 * 60 * 1000L;
        // Round up to the nearest 15-min boundary
        long start = ((now + quarterMs - 1) / quarterMs) * quarterMs;

        for (int i = 0; i < 20; i++) {
            timeSlots.add(new Date(start + (long) i * quarterMs));
        }
    }

    // ── Grid adapter ──────────────────────────────────────────────────────────

    private void setupGrid() {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());

        String[] labels = new String[timeSlots.size()];
        for (int i = 0; i < timeSlots.size(); i++) {
            labels[i] = sdf.format(timeSlots.get(i));
        }

        TimeSlotAdapter adapter = new TimeSlotAdapter(labels);
        gridTimeSlots.setAdapter(adapter);

        gridTimeSlots.setOnItemClickListener((parent, v, position, id) -> {
            Date tapped = timeSlots.get(position);
            // Toggle selection (mirrors Angular selectTimeSlot)
            if (tapped.equals(selectedTime)) {
                selectedTime = null;
            } else {
                selectedTime = tapped;
            }
            adapter.setSelectedPosition(selectedTime == null ? -1 : position);
            adapter.notifyDataSetChanged();
            updateConfirmButton();
        });
    }

    // ── TimePicker ────────────────────────────────────────────────────────────

    private void setupTimePicker() {
        timePicker.setIs24HourView(true);
        timePicker.setOnTimeChangedListener((view, hourOfDay, minute) -> {
            // Mirrors Angular onCustomTimeChange()
            Calendar now = Calendar.getInstance();
            Calendar picked = Calendar.getInstance();
            picked.set(Calendar.HOUR_OF_DAY, hourOfDay);
            picked.set(Calendar.MINUTE, minute);
            picked.set(Calendar.SECOND, 0);
            picked.set(Calendar.MILLISECOND, 0);

            // If the chosen time is already past for today → bump to tomorrow
            if (picked.before(now)) {
                picked.add(Calendar.DAY_OF_YEAR, 1);
            }

            selectedTime = picked.getTime();
            updateConfirmButton();
        });
    }

    // ── Buttons ───────────────────────────────────────────────────────────────

    private void setupButtons() {
        btnToggleMode.setOnClickListener(v -> {
            isCustomMode = !isCustomMode;
            selectedTime = null;

            panelQuickSlots.setVisibility(isCustomMode ? View.GONE  : View.VISIBLE);
            panelCustomTime.setVisibility(isCustomMode ? View.VISIBLE : View.GONE);
            btnToggleMode.setText(isCustomMode ? "Back to quick slots" : "Enter time manually");

            updateConfirmButton();
        });

        btnConfirmTime.setOnClickListener(v -> {
            if (selectedTime != null && listener != null) {
                listener.onTimeSelected(selectedTime);
            }
            dismiss();
        });

        updateConfirmButton();
    }

    private void updateConfirmButton() {
        btnConfirmTime.setEnabled(selectedTime != null);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Inner adapter for the time-slot grid
    // ─────────────────────────────────────────────────────────────────────────

    private class TimeSlotAdapter extends ArrayAdapter<String> {

        private int selectedPosition = -1;

        TimeSlotAdapter(String[] items) {
            super(requireContext(), R.layout.item_time_slot, items);
        }

        void setSelectedPosition(int pos) {
            this.selectedPosition = pos;
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext())
                        .inflate(R.layout.item_time_slot, parent, false);
            }

            TextView tv = convertView.findViewById(R.id.tvTimeSlot);
            tv.setText(getItem(position));

            boolean active = (position == selectedPosition);
            tv.setSelected(active); // drives the selector drawable state
            tv.setTextColor(getResources().getColor(
                    active ? R.color.white : R.color.gray_600, null));

            return convertView;
        }
    }
}