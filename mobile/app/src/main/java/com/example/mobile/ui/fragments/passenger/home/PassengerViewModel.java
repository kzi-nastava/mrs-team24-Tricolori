package com.example.mobile.ui.fragments.passenger.home;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.mobile.model.RideAssignmentResponse;

public class PassengerViewModel extends ViewModel {

    /** Floating "Order Ride" button */
    public static final String STATE_HOME = "HOME";

    /** Order ride panel */
    public static final String STATE_ORDER = "ORDER";

    /** Displayed route, driver info & route details and "Cancel Ride" button */
    public static final String STATE_WAITING = "WAITING";

    /** Ongoing ride */
    public static final String STATE_TRACKING = "TRACKING";


    private final MutableLiveData<String> rideStatus = new MutableLiveData<>(STATE_HOME);
    private final MutableLiveData<RideAssignmentResponse> activeRide = new MutableLiveData<>();

    public LiveData<String> getRideStatus() { return rideStatus; }

    public void setRideStatus(String status) { rideStatus.setValue(status); }

    public LiveData<RideAssignmentResponse> getActiveRide() { return activeRide; }

    public void updateActiveRide(RideAssignmentResponse ride) {
        activeRide.setValue(ride);
        setRideStatus(STATE_WAITING);
    }

    public void clearActiveRide() {
        activeRide.setValue(null);
        setRideStatus(STATE_HOME);
    }
}