package com.example.mobile.ui.fragments.driver.home;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.mobile.model.RideAssignmentResponse;

public class DriverViewModel extends ViewModel {
    public static final String STATE_WAITING = "WAITING";
    public static final String STATE_ASSIGNED = "ASSIGNED";
    public static final String STATE_CANCEL_RIDE = "CANCEL_RIDE";
    public static final String STATE_ACTIVE_RIDE = "ACTIVE_RIDE";

    private final MutableLiveData<String> rideStatus = new MutableLiveData<>(STATE_WAITING);

    private final MutableLiveData<RideAssignmentResponse> activeRide = new MutableLiveData<>();

    public LiveData<String> getRideStatus() {
        return rideStatus;
    }

    public void setRideStatus(String status) {
        rideStatus.setValue(status);
    }

    public LiveData<RideAssignmentResponse> getActiveRide() {
        return activeRide;
    }

    public void updateActiveRide(RideAssignmentResponse ride) {
        activeRide.setValue(ride);
        setRideStatus(STATE_ASSIGNED);
    }

    public void clearActiveRide() {
        activeRide.setValue(null);
        setRideStatus(STATE_WAITING);
    }
}