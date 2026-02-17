package com.example.mobile.ui.fragments.driver.home.components;

import android.os.Bundle;
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

    private DriverViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_driver_waiting, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity()).get(DriverViewModel.class);

        View pulseView = view.findViewById(R.id.pulseView);
        startPulseAnimation(pulseView);

        view.postDelayed(() -> {
            if (isAdded()) {
                RideAssignmentResponse mockRide = createMockRide();
                viewModel.updateActiveRide(mockRide);
            }
        }, 5000);
    }

    private void startPulseAnimation(View view) {
        Animation pulse = AnimationUtils.loadAnimation(getContext(), R.anim.pulse_ring);
        view.startAnimation(pulse);
    }

    private RideAssignmentResponse createMockRide() {
        RideAssignmentResponse ride = new RideAssignmentResponse();

        ride.passengerFirstName = "Mali";
        ride.passengerLastName = "Bobi";
        ride.passengerPhoneNum = "+381 65 123 456";

        ride.pickupAddress = "Mise Dimitrijevica 40";
        ride.destinationAddress = "Lasla Gala 21";
        ride.price = 480.0;
        ride.distanceKm = 0.78;

        ride.routeGeometry = "gscsGsb`xBkIiIMMOOGGIIKIGGYYII_A{@OOk@i@m@k@KIEEGIFS`ByEDIHWNq@VeAPe@BIDKBInAyDJMMO_AkA";
        return ride;
    }
}