package com.example.mobile.ui.fragments;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.TypedValue;
import android.view.Gravity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.cardview.widget.CardView;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class RideHistory extends Fragment {

    private TextInputEditText etStartDate, etEndDate;
    private Button btnFilter;
    private RecyclerView rvRides;
    private RideHistoryAdapter adapter;

    private List<Ride> allRides;
    private List<Ride> filteredRides;

    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private SimpleDateFormat displayFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());

    public RideHistory() {
        // Required empty constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return createLayout();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        loadMockData();
        setupRecyclerView();
        setupListeners();
    }

    private View createLayout() {
        // Create main container
        ViewGroup mainLayout = new androidx.coordinatorlayout.widget.CoordinatorLayout(requireContext());
        mainLayout.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        mainLayout.setBackgroundColor(Color.parseColor("#F5F5F5"));

        // Create scrollable content
        androidx.core.widget.NestedScrollView scrollView = new androidx.core.widget.NestedScrollView(requireContext());
        scrollView.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        scrollView.setFillViewport(true);

        // Create vertical linear layout for content
        android.widget.LinearLayout contentLayout = new android.widget.LinearLayout(requireContext());
        contentLayout.setOrientation(android.widget.LinearLayout.VERTICAL);
        contentLayout.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        int padding = dpToPx(16);
        contentLayout.setPadding(padding, padding, padding, padding);

        // Create filter card
        CardView filterCard = createFilterCard();
        contentLayout.addView(filterCard);

        // Add spacing
        View spacer = new View(requireContext());
        android.widget.LinearLayout.LayoutParams spacerParams = new android.widget.LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dpToPx(requireContext(), 16));
        spacer.setLayoutParams(spacerParams);
        contentLayout.addView(spacer);

        // Create RecyclerView
        rvRides = new RecyclerView(requireContext());
        android.widget.LinearLayout.LayoutParams rvParams = new android.widget.LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        rvRides.setLayoutParams(rvParams);
        rvRides.setId(View.generateViewId());
        contentLayout.addView(rvRides);

        scrollView.addView(contentLayout);
        mainLayout.addView(scrollView);

        return mainLayout;
    }

    private CardView createFilterCard() {
        CardView card = new CardView(requireContext());
        android.widget.LinearLayout.LayoutParams cardParams = new android.widget.LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        card.setLayoutParams(cardParams);
        card.setCardElevation(dpToPx(requireContext(), 4));
        card.setRadius(dpToPx(requireContext(), 8));
        int cardPadding = dpToPx(requireContext(), 16);
        card.setContentPadding(cardPadding, cardPadding, cardPadding, cardPadding);

        android.widget.LinearLayout filterLayout = new android.widget.LinearLayout(requireContext());
        filterLayout.setOrientation(android.widget.LinearLayout.VERTICAL);
        filterLayout.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));

        // Title
        TextView title = new TextView(requireContext());
        title.setText("Filter Ride History");
        title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        title.setTypeface(null, Typeface.BOLD);
        title.setTextColor(Color.parseColor("#333333"));
        android.widget.LinearLayout.LayoutParams titleParams = new android.widget.LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        titleParams.setMargins(0, 0, 0, dpToPx(requireContext(), 16));
        title.setLayoutParams(titleParams);
        filterLayout.addView(title);

        // Start Date Input
        TextInputLayout startDateLayout = createTextInputLayout("Start Date");
        etStartDate = createTextInputEditText();
        startDateLayout.addView(etStartDate);
        filterLayout.addView(startDateLayout);

        // End Date Input
        TextInputLayout endDateLayout = createTextInputLayout("End Date");
        etEndDate = createTextInputEditText();
        endDateLayout.addView(etEndDate);
        filterLayout.addView(endDateLayout);

        // Filter Button
        btnFilter = new Button(requireContext());
        btnFilter.setText("Apply Filter");
        btnFilter.setTextColor(Color.WHITE);
        btnFilter.setBackgroundColor(Color.parseColor("#2196F3"));
        android.widget.LinearLayout.LayoutParams btnParams = new android.widget.LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        btnParams.setMargins(0, dpToPx(requireContext(), 16), 0, 0);
        btnFilter.setLayoutParams(btnParams);
        filterLayout.addView(btnFilter);

        card.addView(filterLayout);
        return card;
    }

    private TextInputLayout createTextInputLayout(String hint) {
        TextInputLayout layout = new TextInputLayout(requireContext());
        android.widget.LinearLayout.LayoutParams params = new android.widget.LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 0, 0, dpToPx(requireContext(), 12));
        layout.setLayoutParams(params);
        layout.setHint(hint);
        return layout;
    }

    private TextInputEditText createTextInputEditText() {
        TextInputEditText editText = new TextInputEditText(requireContext());
        editText.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        editText.setFocusable(false);
        editText.setClickable(true);
        editText.setId(View.generateViewId());
        return editText;
    }

    private int dpToPx(int dp) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, dp,
                getResources().getDisplayMetrics());
    }

    private static int dpToPx(android.content.Context context, int dp) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, dp,
                context.getResources().getDisplayMetrics());
    }

    private void initViews(View view) {
        // Views are already initialized in createLayout()
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

    // Inner Ride Model Class
    public static class Ride {
        private int id;
        private String route;
        private String startDate;
        private String endDate;
        private double price;
        private String status;
        private String startTime;
        private String endTime;
        private String duration;
        private String passengerName;
        private String passengerPhone;
        private double distance;
        private String paymentMethod;
        private String notes;

        public Ride(int id, String route, String startDate, String endDate, double price,
                    String status, String startTime, String endTime, String duration,
                    String passengerName, String passengerPhone, double distance,
                    String paymentMethod, String notes) {
            this.id = id;
            this.route = route;
            this.startDate = startDate;
            this.endDate = endDate;
            this.price = price;
            this.status = status;
            this.startTime = startTime;
            this.endTime = endTime;
            this.duration = duration;
            this.passengerName = passengerName;
            this.passengerPhone = passengerPhone;
            this.distance = distance;
            this.paymentMethod = paymentMethod;
            this.notes = notes;
        }

        // Getters
        public int getId() { return id; }
        public String getRoute() { return route; }
        public String getStartDate() { return startDate; }
        public String getEndDate() { return endDate; }
        public double getPrice() { return price; }
        public String getStatus() { return status; }
        public String getStartTime() { return startTime; }
        public String getEndTime() { return endTime; }
        public String getDuration() { return duration; }
        public String getPassengerName() { return passengerName; }
        public String getPassengerPhone() { return passengerPhone; }
        public double getDistance() { return distance; }
        public String getPaymentMethod() { return paymentMethod; }
        public String getNotes() { return notes; }
    }

    // Inner Adapter Class
    private static class RideHistoryAdapter extends RecyclerView.Adapter<RideHistoryAdapter.RideViewHolder> {
        private List<Ride> rides;
        private OnRideClickListener listener;

        interface OnRideClickListener {
            void onRideClick(Ride ride);
        }

        public RideHistoryAdapter(List<Ride> rides, OnRideClickListener listener) {
            this.rides = rides;
            this.listener = listener;
        }

        public void updateData(List<Ride> newRides) {
            this.rides = newRides;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public RideViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            CardView cardView = new CardView(parent.getContext());
            android.widget.LinearLayout.LayoutParams params = new android.widget.LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            int margin = dpToPx(parent.getContext(), 8);
            params.setMargins(0, margin, 0, margin);
            cardView.setLayoutParams(params);
            cardView.setCardElevation(dpToPx(parent.getContext(), 2));
            cardView.setRadius(dpToPx(parent.getContext(), 8));
            int padding = dpToPx(parent.getContext(), 16);
            cardView.setContentPadding(padding, padding, padding, padding);

            return new RideViewHolder(cardView);
        }

        @Override
        public void onBindViewHolder(@NonNull RideViewHolder holder, int position) {
            holder.bind(rides.get(position));
        }

        @Override
        public int getItemCount() {
            return rides.size();
        }

        class RideViewHolder extends RecyclerView.ViewHolder {
            private TextView tvRoute, tvDate, tvPrice, tvStatus, tvPassenger;
            private int dpToPxValue;

            public RideViewHolder(@NonNull CardView cardView) {
                super(cardView);

                // Store context for dpToPx calculations
                final android.content.Context ctx = cardView.getContext();

                android.widget.LinearLayout layout = new android.widget.LinearLayout(ctx);
                layout.setOrientation(android.widget.LinearLayout.VERTICAL);
                layout.setLayoutParams(new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

                tvRoute = createTextView(ctx, 18, Typeface.BOLD, "#333333");
                tvDate = createTextView(ctx, 14, Typeface.NORMAL, "#666666");
                tvPrice = createTextView(ctx, 16, Typeface.BOLD, "#2196F3");
                tvStatus = createTextView(ctx, 14, Typeface.NORMAL, "#4CAF50");
                tvPassenger = createTextView(ctx, 14, Typeface.NORMAL, "#666666");

                layout.addView(tvRoute);
                layout.addView(createSpacer(ctx, 4));
                layout.addView(tvDate);
                layout.addView(createSpacer(ctx, 8));

                android.widget.LinearLayout bottomRow = new android.widget.LinearLayout(ctx);
                bottomRow.setOrientation(android.widget.LinearLayout.HORIZONTAL);
                bottomRow.setLayoutParams(new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

                android.widget.LinearLayout leftColumn = new android.widget.LinearLayout(ctx);
                leftColumn.setOrientation(android.widget.LinearLayout.VERTICAL);
                android.widget.LinearLayout.LayoutParams leftParams = new android.widget.LinearLayout.LayoutParams(
                        0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
                leftColumn.setLayoutParams(leftParams);
                leftColumn.addView(tvPrice);
                leftColumn.addView(createSpacer(ctx, 4));
                leftColumn.addView(tvPassenger);

                tvStatus.setGravity(Gravity.END);
                android.widget.LinearLayout.LayoutParams statusParams = new android.widget.LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                tvStatus.setLayoutParams(statusParams);

                bottomRow.addView(leftColumn);
                bottomRow.addView(tvStatus);

                layout.addView(bottomRow);
                cardView.addView(layout);

                cardView.setOnClickListener(v -> {
                    int pos = getAdapterPosition();
                    if (pos != RecyclerView.NO_POSITION && listener != null) {
                        listener.onRideClick(rides.get(pos));
                    }
                });
            }

            private TextView createTextView(android.content.Context ctx, int textSize, int style, String color) {
                TextView tv = new TextView(ctx);
                tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize);
                tv.setTypeface(null, style);
                tv.setTextColor(Color.parseColor(color));
                tv.setLayoutParams(new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                return tv;
            }

            private View createSpacer(android.content.Context ctx, int dp) {
                View spacer = new View(ctx);
                spacer.setLayoutParams(new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, RideHistory.dpToPx(ctx, dp)));
                return spacer;
            }

            private int dpToPx(android.content.Context ctx, int dp) {
                return RideHistory.dpToPx(ctx, dp);
            }

            public void bind(Ride ride) {
                tvRoute.setText(ride.getRoute());
                tvDate.setText(ride.getStartDate() + " • " + ride.getStartTime());
                tvPrice.setText(String.format(Locale.getDefault(), "€%.2f", ride.getPrice()));
                tvStatus.setText(ride.getStatus());
                tvPassenger.setText(ride.getPassengerName());

                if ("Completed".equals(ride.getStatus())) {
                    tvStatus.setTextColor(Color.parseColor("#4CAF50"));
                } else if ("Cancelled".equals(ride.getStatus())) {
                    tvStatus.setTextColor(Color.parseColor("#F44336"));
                } else {
                    tvStatus.setTextColor(Color.parseColor("#FF9800"));
                }
            }
        }
    }

    // Inner Dialog Fragment Class
    public static class RideDetailsDialogFragment extends DialogFragment {
        private static final String ARG_RIDE = "ride";

        public static RideDetailsDialogFragment newInstance(Ride ride) {
            RideDetailsDialogFragment fragment = new RideDetailsDialogFragment();
            Bundle args = new Bundle();
            args.putInt("id", ride.getId());
            args.putString("route", ride.getRoute());
            args.putString("startDate", ride.getStartDate());
            args.putString("endDate", ride.getEndDate());
            args.putDouble("price", ride.getPrice());
            args.putString("status", ride.getStatus());
            args.putString("startTime", ride.getStartTime());
            args.putString("endTime", ride.getEndTime());
            args.putString("duration", ride.getDuration());
            args.putString("passengerName", ride.getPassengerName());
            args.putString("passengerPhone", ride.getPassengerPhone());
            args.putDouble("distance", ride.getDistance());
            args.putString("paymentMethod", ride.getPaymentMethod());
            args.putString("notes", ride.getNotes());
            fragment.setArguments(args);
            return fragment;
        }

        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                                 @Nullable Bundle savedInstanceState) {
            Bundle args = getArguments();
            if (args == null) return null;

            androidx.core.widget.NestedScrollView scrollView = new androidx.core.widget.NestedScrollView(requireContext());
            scrollView.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

            android.widget.LinearLayout layout = new android.widget.LinearLayout(requireContext());
            layout.setOrientation(android.widget.LinearLayout.VERTICAL);
            int padding = dpToPx(requireContext(), 24);
            layout.setPadding(padding, padding, padding, padding);
            layout.setBackgroundColor(Color.WHITE);

            // Title
            TextView title = new TextView(requireContext());
            title.setText("Ride Details");
            title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24);
            title.setTypeface(null, Typeface.BOLD);
            title.setTextColor(Color.parseColor("#333333"));
            layout.addView(title);
            layout.addView(createSpacer(16));

            // Add all details
            addDetailRow(layout, "Route", args.getString("route"));
            addDetailRow(layout, "Date", args.getString("startDate"));
            addDetailRow(layout, "Start Time", args.getString("startTime"));
            addDetailRow(layout, "End Time", args.getString("endTime"));
            addDetailRow(layout, "Duration", args.getString("duration"));
            addDetailRow(layout, "Distance", String.format(Locale.getDefault(), "%.1f km", args.getDouble("distance")));
            addDetailRow(layout, "Price", String.format(Locale.getDefault(), "€%.2f", args.getDouble("price")));
            addDetailRow(layout, "Payment Method", args.getString("paymentMethod"));
            addDetailRow(layout, "Status", args.getString("status"));
            addDetailRow(layout, "Passenger", args.getString("passengerName"));
            addDetailRow(layout, "Phone", args.getString("passengerPhone"));

            String notes = args.getString("notes");
            if (notes != null && !notes.isEmpty()) {
                addDetailRow(layout, "Notes", notes);
            }

            // Close button
            Button closeButton = new Button(requireContext());
            closeButton.setText("Close");
            closeButton.setTextColor(Color.WHITE);
            closeButton.setBackgroundColor(Color.parseColor("#2196F3"));
            android.widget.LinearLayout.LayoutParams btnParams = new android.widget.LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            btnParams.setMargins(0, dpToPx(requireContext(), 16), 0, 0);
            closeButton.setLayoutParams(btnParams);
            closeButton.setOnClickListener(v -> dismiss());
            layout.addView(closeButton);

            scrollView.addView(layout);
            return scrollView;
        }

        private void addDetailRow(android.widget.LinearLayout parent, String label, String value) {
            android.widget.LinearLayout row = new android.widget.LinearLayout(requireContext());
            row.setOrientation(android.widget.LinearLayout.VERTICAL);
            android.widget.LinearLayout.LayoutParams rowParams = new android.widget.LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            rowParams.setMargins(0, 0, 0, dpToPx(requireContext(), 12));
            row.setLayoutParams(rowParams);

            TextView labelView = new TextView(requireContext());
            labelView.setText(label);
            labelView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
            labelView.setTextColor(Color.parseColor("#999999"));
            labelView.setTypeface(null, Typeface.BOLD);

            TextView valueView = new TextView(requireContext());
            valueView.setText(value);
            valueView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
            valueView.setTextColor(Color.parseColor("#333333"));

            row.addView(labelView);
            row.addView(createSpacer(4));
            row.addView(valueView);

            parent.addView(row);
        }

        private View createSpacer(int dp) {
            View spacer = new View(requireContext());
            spacer.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, dpToPx(requireContext(), dp)));
            return spacer;
        }

        private int dpToPx(android.content.Context ctx, int dp) {
            return (int) TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP, dp,
                    ctx.getResources().getDisplayMetrics());
        }
    }
}