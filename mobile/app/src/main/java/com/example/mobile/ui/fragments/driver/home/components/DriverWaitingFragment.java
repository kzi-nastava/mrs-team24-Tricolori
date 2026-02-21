package com.example.mobile.ui.fragments.driver.home.components;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.example.mobile.R;
import com.example.mobile.model.RideAssignmentResponse;
import com.example.mobile.ui.fragments.driver.home.DriverViewModel;

import org.osmdroid.bonuspack.utils.PolylineEncoder;
import org.osmdroid.util.GeoPoint;

import java.util.ArrayList;

public class DriverWaitingFragment extends Fragment {
    private static final String TAG = "DriverWaiting";
    private DriverViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_driver_waiting, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // ============ SETUP VIEWMODEL ============
        viewModel = new ViewModelProvider(requireActivity()).get(DriverViewModel.class);

        // ============ PULSE ANIMATION ============
        View pulseView = view.findViewById(R.id.pulseView);
        startPulseAnimation(pulseView);

        // ============ POČNI SLUŠATI RIDE ASSIGNMENTS ============
        // 🆕 OVO JE GLAVNA LINIJA
        viewModel.startListeningForRideAssignments();

        // ============ SLUŠAJ RIDE ASSIGNMENTS ============
        viewModel.getActiveRide().observe(getViewLifecycleOwner(), ride -> {
            if (ride != null) {
                Log.d(TAG, "Ride received!");
                // DriverHomeFragment će viditi STATE_ASSIGNED i prikazati detalje
            }
        });
    }

    private void startPulseAnimation(View view) {
        Animation pulse = AnimationUtils.loadAnimation(getContext(), R.anim.pulse_ring);
        view.startAnimation(pulse);
    }
}