package com.example.mobile.ui.fragments.passenger.home;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.mobile.model.RideAssignmentResponse;
import com.example.mobile.network.service.RideService;
import com.example.mobile.network.service.StompRideService;

public class PassengerViewModel extends AndroidViewModel {

    private static final String TAG = "PassengerViewModel";

    public static final String STATE_HOME = "HOME";
    public static final String STATE_ORDER = "ORDER";
    public static final String STATE_WAITING = "WAITING";
    public static final String STATE_TRACKING = "TRACKING";

    // ============ ISTA LIVEDATA KAO PRIJE ============
    private final MutableLiveData<String> rideStatus = new MutableLiveData<>(STATE_HOME);
    private final MutableLiveData<RideAssignmentResponse> activeRide = new MutableLiveData<>();

    // ============ NOVO: Services ============
    private SharedPreferences sharedPreferences;

    private StompRideService stompRideService;
    private final Application application;

    public PassengerViewModel(@NonNull Application application) {
        super(application);
        this.application = application;
        this.sharedPreferences = application.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        this.stompRideService = new StompRideService();  // ← Kreiraj običnu instancu
    }

    // ============ EXISTING METHODS (NE MIJENJAJ) ============

    public LiveData<String> getRideStatus() {
        return rideStatus;
    }

    public void setRideStatus(String status) {
        rideStatus.setValue(status);
    }

    public LiveData<RideAssignmentResponse> getActiveRide() {
        return activeRide;
    }

    /**
     * AŽURIRAN - Dodaj WebSocket notifikacije
     */
    public void updateActiveRide(RideAssignmentResponse ride) {
        Log.d(TAG, "updateActiveRide: " + (ride != null ? ride.id : "null"));
        activeRide.setValue(ride);
        setRideStatus(STATE_WAITING);

        // 🆕 Poveži se na WebSocket
        if (ride != null && ride.id != null) {
            connectWebSocketForRideUpdates();
        }
    }

    /**
     * Obrisi voznju
     */
    public void clearActiveRide() {
        Log.d(TAG, "clearActiveRide");
        activeRide.setValue(null);
        setRideStatus(STATE_HOME);

        // 🆕 Prekini WebSocket
        if (stompRideService != null) {
            stompRideService.disconnect();
        }
    }

    // ============ NOVO: WebSocket ============

    /**
     * Poveži se na WebSocket za ride status notifikacije
     */
    private void connectWebSocketForRideUpdates() {
        Log.d(TAG, "connectWebSocketForRideUpdates");

        String email = sharedPreferences.getString("user_email", "");
        String jwt = sharedPreferences.getString("jwt_token", "");

        Log.d(TAG, "email: '" + email + "'");
        Log.d(TAG, "jwt empty: " + jwt.isEmpty());
        Log.d(TAG, "jwt length: " + jwt.length());

        if (email.isEmpty() || jwt.isEmpty()) {
            Log.e(TAG, "❌ Missing credentials");
            return;
        }

        // 📡 Poveži se na WebSocket
        stompRideService.connect(email, jwt, new StompRideService.RideListener() {

            @Override
            public void onRideAssigned(long rideId) {
                // Putnik ignoriše
            }

            @Override
            public void onRideStatusUpdate(StompRideService.RideStatusUpdate update) {
                if (update == null || update.getStatus() == null) return;

                Log.d(TAG, "🔔 Ride status: " + update.getStatus());

                String status = update.getStatus();

                if ("ONGOING".equalsIgnoreCase(status)) {
                    setRideStatus(STATE_TRACKING);
                } else if ("CANCELLED_BY_DRIVER".equalsIgnoreCase(status) ||
                        "CANCELLED_BY_PASSENGER".equalsIgnoreCase(status) ||
                        "CANCELLED".equalsIgnoreCase(status)) {
                    clearActiveRide();
                } else if ("FINISHED".equalsIgnoreCase(status)) {
                    setRideStatus(STATE_HOME);
                }
            }

            @Override
            public void onConnectionStateChanged(boolean connected) {
                if (connected) {
                    Log.d(TAG, "✅ WebSocket CONNECTED uspješno!");
                } else {
                    Log.e(TAG, "❌ WebSocket DISCONNECTED");
                }
            }
        });
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (stompRideService != null) {
            stompRideService.disconnect();
        }
    }
}
