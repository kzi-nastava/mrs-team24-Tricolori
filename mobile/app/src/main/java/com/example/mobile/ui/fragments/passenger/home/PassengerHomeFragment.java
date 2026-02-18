package com.example.mobile.ui.fragments.passenger.home;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.PreferenceManager;

import com.example.mobile.R;
import com.example.mobile.model.RideAssignmentResponse;
import com.example.mobile.ui.components.MapComponent;
import com.example.mobile.ui.fragments.passenger.home.components.PassengerOrderFragment;
import com.example.mobile.ui.fragments.passenger.home.components.PassengerWaitingFragment;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;

public class PassengerHomeFragment extends Fragment {

    private MapComponent mapComponent;
    private MapView mapView;
    private PassengerViewModel viewModel;
    private ExtendedFloatingActionButton btnOrderRide;

    private static final double NS_LAT = 45.2671;
    private static final double NS_LON = 19.8335;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                String status = viewModel.getRideStatus().getValue();
                if (PassengerViewModel.STATE_ORDER.equals(status)) {
                    viewModel.setRideStatus(PassengerViewModel.STATE_HOME);
                } else {
                    setEnabled(false);
                    requireActivity().getOnBackPressedDispatcher().onBackPressed();
                }
            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(this, callback);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Context ctx = requireContext();
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));
        Configuration.getInstance().setUserAgentValue(ctx.getPackageName());
        return inflater.inflate(R.layout.fragment_passenger_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mapView = view.findViewById(R.id.map);
        btnOrderRide = view.findViewById(R.id.btnOrderRide);
        setupMapView();

        mapComponent = new MapComponent(mapView, requireContext());
        viewModel = new ViewModelProvider(requireActivity()).get(PassengerViewModel.class);

        if (PassengerViewModel.STATE_ORDER.equals(viewModel.getRideStatus().getValue())) {
            viewModel.setRideStatus(PassengerViewModel.STATE_HOME);
        }

        btnOrderRide.setOnClickListener(v -> {
            viewModel.setRideStatus(PassengerViewModel.STATE_ORDER);
        });

        viewModel.getRideStatus().observe(getViewLifecycleOwner(), this::handleStateChange);

        viewModel.getActiveRide().observe(getViewLifecycleOwner(), ride -> {
            if (ride != null && ride.routeGeometry != null) {
                mapComponent.drawRoute(ride.routeGeometry);
            } else {
                mapComponent.clearRouteAndMarkers();
                mapView.getController().animateTo(new GeoPoint(NS_LAT, NS_LON));
            }
        });
    }

    private void setupMapView() {
        if (mapView == null) return;
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setMultiTouchControls(true);
        mapView.setBuiltInZoomControls(false);
        mapView.getController().setZoom(14.5);
        mapView.getController().setCenter(new GeoPoint(NS_LAT, NS_LON));
    }

    private void handleStateChange(String status) {
        if (status == null) return;

        if (status.equals(PassengerViewModel.STATE_HOME)) {
            btnOrderRide.setVisibility(View.VISIBLE);
            clearPanel();
        } else {
            btnOrderRide.setVisibility(View.GONE);
            Fragment fragment = null;

            if (status.equals(PassengerViewModel.STATE_ORDER)) {
                fragment = new PassengerOrderFragment();
            } else if (status.equals(PassengerViewModel.STATE_WAITING)) {
                fragment = new PassengerWaitingFragment();
            } else if (status.equals(PassengerViewModel.STATE_TRACKING)) {
                RideAssignmentResponse activeRide = viewModel.getActiveRide().getValue();
                if (activeRide != null && activeRide.id != null) {
                    navigateToTracking(activeRide.id);
                }
                return;
            }

            if (fragment != null) {
                replacePanel(fragment);
            }
        }
    }

    private void replacePanel(Fragment fragment) {
        getChildFragmentManager()
                .beginTransaction()
                .setCustomAnimations(R.anim.slide_in_up, R.anim.fade_out, R.anim.fade_in, R.anim.slide_out_down)
                .replace(R.id.panelContainer, fragment)
                .commit();
    }

    private void clearPanel() {
        Fragment current = getChildFragmentManager().findFragmentById(R.id.panelContainer);
        if (current != null) {
            getChildFragmentManager().beginTransaction()
                    .setCustomAnimations(0, R.anim.slide_out_down)
                    .remove(current)
                    .commit();
        }
    }

    @Override public void onResume() { super.onResume(); if (mapView != null) mapView.onResume(); }
    @Override public void onPause() { super.onPause(); if (mapView != null) mapView.onPause(); }

    private void navigateToTracking(Long rideId) {
        Bundle args = new Bundle();
        args.putLong("ride_id", rideId);

        androidx.navigation.NavController navController =
                androidx.navigation.Navigation.findNavController(requireActivity(), R.id.nav_host_fragment);
        navController.navigate(R.id.passengerRideTrackingFragment, args);
    }
}