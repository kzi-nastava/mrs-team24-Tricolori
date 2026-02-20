package com.example.mobile.ui.fragments.driver.home;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.PreferenceManager;

import com.example.mobile.R;
import com.example.mobile.model.RideAssignmentResponse;
import com.example.mobile.ui.fragments.driver.home.components.DriverCancelRideFragment;
import com.example.mobile.ui.components.MapComponent;
import com.example.mobile.ui.fragments.driver.home.components.DriverRideAssignmentFragment;
import com.example.mobile.ui.fragments.driver.home.components.DriverRideTrackingFragment;
import com.example.mobile.ui.fragments.driver.home.components.DriverWaitingFragment;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;

public class DriverHomeFragment extends Fragment {

    private MapComponent mapComponent;
    private MapView mapView;

    private static final double NS_LAT = 45.2671;
    private static final double NS_LON = 19.8335;

    private DriverViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Context ctx = requireContext();

        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));
        Configuration.getInstance().setUserAgentValue(ctx.getPackageName());

        return inflater.inflate(R.layout.fragment_driver_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mapView = view.findViewById(R.id.map);
        setupMapView();

        mapComponent = new MapComponent(mapView, requireContext());

        viewModel = new ViewModelProvider(requireActivity()).get(DriverViewModel.class);

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
        GeoPoint startPoint = new GeoPoint(NS_LAT, NS_LON);
        mapView.getController().setCenter(startPoint);
    }

    private void handleStateChange(String status) {
        Fragment fragment;

        switch (status) {
            case DriverViewModel.STATE_ASSIGNED:
                fragment = new DriverRideAssignmentFragment();
                break;
            case DriverViewModel.STATE_CANCEL_RIDE:
                fragment = new DriverCancelRideFragment();
                break;
            case DriverViewModel.STATE_ACTIVE_RIDE:
                RideAssignmentResponse ride = viewModel.getActiveRide().getValue();
                if (ride != null && ride.id != null) {
                    replacePanel(DriverRideTrackingFragment.newInstance(ride.id));
                }
                return;
            case DriverViewModel.STATE_WAITING:
            default:
                if (mapComponent != null) mapComponent.clearRouteAndMarkers();
                fragment = new DriverWaitingFragment();
                break;
        }

        replacePanel(fragment);
    }

    private void replacePanel(Fragment fragment) {
        getChildFragmentManager()
                .beginTransaction()
                .setCustomAnimations(
                        R.anim.slide_in_up,
                        R.anim.fade_out,
                        R.anim.fade_in,
                        R.anim.slide_out_down
                )
                .replace(R.id.panelContainer, fragment)
                .commit();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mapView != null) mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mapView != null) mapView.onPause();
    }
}