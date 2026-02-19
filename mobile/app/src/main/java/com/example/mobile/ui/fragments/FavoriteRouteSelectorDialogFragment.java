package com.example.mobile.ui.fragments;

import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mobile.R;
import com.example.mobile.dto.ride.FavoriteRoute;
import com.example.mobile.network.RetrofitClient;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FavoriteRouteSelectorDialogFragment extends DialogFragment {
    public static final String TAG = "FavoriteRouteSelectorDialog";

    // ── Listener ──────────────────────────────────────────────────────────────

    public interface OnRouteSelectedListener {
        void onRouteSelected(FavoriteRoute route);
    }

    private OnRouteSelectedListener listener;

    public void setListener(OnRouteSelectedListener listener) {
        this.listener = listener;
    }

    // ── Views ─────────────────────────────────────────────────────────────────

    private LinearLayout routesContainer;

    // ─────────────────────────────────────────────────────────────────────────

    public FavoriteRouteSelectorDialogFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_favorite_route_selector_dialog, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        routesContainer  = view.findViewById(R.id.routesContainer);

        ImageButton btnClose = view.findViewById(R.id.btnClose);
        btnClose.setOnClickListener(v -> dismiss());

        loadFavoriteRoutes();
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null && dialog.getWindow() != null) {
            // Full-width, 75% of screen height — mirrors Angular h-[75vh]
            int screenHeight = getResources().getDisplayMetrics().heightPixels;
            dialog.getWindow().setLayout(
                    (int) (getResources().getDisplayMetrics().widthPixels * 0.94),
                    (int) (screenHeight * 0.75));
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
    }

    // ── Data loading ──────────────────────────────────────────────────────────

    private void loadFavoriteRoutes() {
        RetrofitClient.getFavoriteRoutesService(requireContext()).getFavoriteRoutes().enqueue(new Callback<List<FavoriteRoute>>() {
            @Override
            public void onResponse(@NonNull Call<List<FavoriteRoute>> call,
                                   @NonNull Response<List<FavoriteRoute>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    populateRoutes(response.body());
                } else {
                    Toast.makeText(requireContext(),
                            "Greška pri učitavanju favorita", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<FavoriteRoute>> call,
                                  @NonNull Throwable t) {
                Toast.makeText(requireContext(),
                        "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void populateRoutes(List<FavoriteRoute> routes) {
        routesContainer.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(requireContext());

        for (FavoriteRoute route : routes) {
            View card = inflater.inflate(R.layout.item_favorite_route, routesContainer, false);

            TextView tvTitle       = card.findViewById(R.id.tvRouteTitle);
            TextView tvPickup      = card.findViewById(R.id.tvPickupAddress);
            TextView tvDestination = card.findViewById(R.id.tvDestinationAddress);

            tvTitle.setText(route.getTitle());

            if (route.getRoute().getPickupStop() != null) {
                tvPickup.setText(route.getRoute().getPickupStop().getAddress());
            }
            if (route.getRoute().getDestinationStop() != null) {
                tvDestination.setText(route.getRoute().getDestinationStop().getAddress());
            }

            card.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onRouteSelected(route);
                }
                dismiss();
            });

            routesContainer.addView(card);
        }
    }
}