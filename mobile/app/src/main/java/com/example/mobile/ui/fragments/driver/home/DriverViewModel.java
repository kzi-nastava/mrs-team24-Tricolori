package com.example.mobile.ui.fragments.driver.home;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.mobile.dto.ride.DriverRideDetailResponse;
import com.example.mobile.model.RideAssignmentResponse;
import com.example.mobile.network.RetrofitClient;
import com.example.mobile.network.service.RideService;
import com.example.mobile.network.service.StompRideService;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DriverViewModel extends AndroidViewModel {
    private static final String TAG = "DriverViewModel";

    public static final String STATE_WAITING = "WAITING";
    public static final String STATE_ASSIGNED = "ASSIGNED";
    public static final String STATE_CANCEL_RIDE = "CANCEL_RIDE";
    public static final String STATE_ACTIVE_RIDE = "ACTIVE_RIDE";

    // ============ ISTA LIVEDATA KAO PRIJE ============
    private final MutableLiveData<String> rideStatus = new MutableLiveData<>(STATE_WAITING);
    private final MutableLiveData<RideAssignmentResponse> activeRide = new MutableLiveData<>();

    // ============ NOVO: Services ============
    private StompRideService stompRideService;
    private SharedPreferences sharedPreferences;
    private final Application application;

    public DriverViewModel(@NonNull Application application) {
        super(application);
        this.application = application;
        // 🆕 Inicijalizuj servise
        this.sharedPreferences = application.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        this.stompRideService = new StompRideService();  // ← Kreiraj običnu instancu
    }

    // ============ EXISTING METHODS (NE MIJENJAJ) ============

    public LiveData<String> getRideStatus() {
        return rideStatus;
    }

    public void setRideStatus(String status) {
        Log.d(TAG, "setRideStatus: " + status);
        rideStatus.setValue(status);
    }

    public LiveData<RideAssignmentResponse> getActiveRide() {
        return activeRide;
    }

    public void updateActiveRide(RideAssignmentResponse ride) {
        Log.d(TAG, "updateActiveRide: " + (ride != null ? ride.id : "null"));
        activeRide.setValue(ride);
        setRideStatus(STATE_ASSIGNED);
    }

    public void clearActiveRide() {
        Log.d(TAG, "clearActiveRide");
        activeRide.setValue(null);
        setRideStatus(STATE_WAITING);
    }

    // ============ NOVO: WebSocket Methods ============

    /**
     * 🆕 Počni slušati ride assignments preko WebSocket-a
     *
     * Šta se dešava:
     * 1. Poveži se na WebSocket
     * 2. Slušaj /user/queue/ride-assigned
     * 3. Kada ride stigne (samo Long rideId), učitaj detalje
     * 4. updateActiveRide() će biti pozvan sa RideAssignmentResponse
     * 5. Fragment će viditi promjenu i prikazati detalje
     */
    public void startListeningForRideAssignments() {
        Log.d(TAG, "🚀 startListeningForRideAssignments called"); // ← dodaj
        if (stompRideService.isConnected()) {
            Log.d(TAG, "Already connected, skipping");
            return;
        }

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

            /**
             * 🎯 KLJUČNA METODA - Vozač dobije ride assignment
             *
             * WebSocket pošalje samo Long (rideId)
             * Mi trebamo učitati sve detalje iz REST API-ja
             */
            @Override
            public void onRideAssigned(long rideId) {
                Log.d(TAG, "🔔 onRideAssigned: " + rideId);
                // Odmah učitaj detalje voznje
                loadRideDetails(rideId);
            }

            /**
             * Vozač ignoriše ride status updates
             * (To je samo za putnike)
             */
            @Override
            public void onRideStatusUpdate(StompRideService.RideStatusUpdate update) {
                Log.v(TAG, "Ride status update (ignoring for driver): " + update.getStatus());
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

    /**
     * 🆕 Učitaj ride detalje kada vozač dobije assignment
     *
     * Parametar: rideId (primljen iz onRideAssigned callback-a)
     *
     * Šta se dešava:
     * 1. REST API poziv: GET /api/rides/{rideId}
     * 2. Dobij RideAssignmentResponse sa svim detaljima
     * 3. updateActiveRide(ride) - ažuriraj LiveData
     * 4. Fragment će viditi promjenu i prikazati ride detalje
     */
    private void loadRideDetails(long rideId) {
        Log.d(TAG, "loadRideDetails: " + rideId);

        RetrofitClient.getRideService(application).getRideAssignment(rideId).enqueue(new Callback<RideAssignmentResponse>() {
            @Override
            public void onResponse(Call<RideAssignmentResponse> call, Response<RideAssignmentResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.d(TAG, "Ride loaded");
                    updateActiveRide(response.body());
                } else {
                    Log.e(TAG, "Failed to load: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<RideAssignmentResponse> call, Throwable t) {
                Log.e(TAG, "Error", t);
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