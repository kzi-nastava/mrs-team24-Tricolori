package com.example.mobile.ui.fragments.history;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.mobile.network.RetrofitClient;
import com.example.mobile.network.service.RideService;
import com.example.mobile.ui.models.Ride;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RideHistoryViewModel extends ViewModel {

    private static final String TAG = "PagedRideHistoryVM";

    public enum Role {
        PASSENGER, ADMIN
    }

    // LiveData for UI state
    private final MutableLiveData<List<Ride>> rides = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<Integer> totalElements = new MutableLiveData<>(0);
    private final MutableLiveData<Integer> currentPage = new MutableLiveData<>(0);

    // Pagination & Sorting
    private int page = 0;
    private int size = 10;
    private String sortBy = "createdAt";
    private String sortDirection = "DESC";

    // Filters
    private String startDate = "";
    private String endDate = "";
    private String personEmail = ""; // For admin only

    // Role
    private Role role;

    // Getters for LiveData
    public LiveData<List<Ride>> getRides() {
        return rides;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public LiveData<Integer> getTotalElements() {
        return totalElements;
    }

    public LiveData<Integer> getCurrentPage() {
        return currentPage;
    }

    public int getSize() {
        return size;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    // Pagination methods
    public void nextPage() {
        Integer total = totalElements.getValue();
        if (total != null && (page + 1) * size < total) {
            page++;
            currentPage.setValue(page);
        }
    }

    public void previousPage() {
        if (page > 0) {
            page--;
            currentPage.setValue(page);
        }
    }

    public void resetPage() {
        page = 0;
        currentPage.setValue(page);
    }

    public boolean canGoNext() {
        Integer total = totalElements.getValue();
        return total != null && (page + 1) * size < total;
    }

    public boolean canGoPrevious() {
        return page > 0;
    }

    // Filter methods
    public void setDateRange(String start, String end) {
        this.startDate = start;
        this.endDate = end;
    }

    public void setPersonEmail(String email) {
        this.personEmail = email;
    }

    public void clearFilters() {
        this.startDate = "";
        this.endDate = "";
        this.personEmail = "";
        this.page = 0;
        currentPage.setValue(page);
    }

    // Sorting
    public void setSorting(String sortBy, String sortDirection) {
        this.sortBy = sortBy;
        this.sortDirection = sortDirection;
        this.page = 0;
        currentPage.setValue(page);
    }

    public String getSortBy() {
        return sortBy;
    }

    public String getSortDirection() {
        return sortDirection;
    }

    // Main load method
    public void loadRideHistory(Context context) {
        isLoading.setValue(true);
        errorMessage.setValue(null);

        SharedPreferences prefs =
                context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        String token = prefs.getString("jwt_token", null);

        if (token == null || token.isEmpty()) {
            isLoading.setValue(false);
            errorMessage.setValue("Please log in first");
            return;
        }

        if (role == Role.PASSENGER) {
            loadPassengerHistory(context);
        } else {
            loadAdminHistory(context);
        }
    }

    private void loadPassengerHistory(Context context) {
        RideService rideService =
                RetrofitClient.getClient(context).create(RideService.class);

        String startDateParam = startDate.isEmpty() ? null : startDate;
        String endDateParam = endDate.isEmpty() ? null : endDate;
        String sortParam = sortBy + "," + sortDirection;

        rideService.getPassengerRideHistory(
                startDateParam,
                endDateParam,
                page,
                size,
                sortParam
        ).enqueue(new Callback<>() {

            @Override
            public void onResponse(Call<ResponseBody> call,
                                   Response<ResponseBody> response) {

                isLoading.setValue(false);

                if (!response.isSuccessful() || response.body() == null) {
                    handleError(response);
                    return;
                }

                parsePagedResponse(response);
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                isLoading.setValue(false);
                errorMessage.setValue("Network error: " + t.getMessage());
                Log.e(TAG, "Passenger history failure", t);
            }
        });
    }

    private void loadAdminHistory(Context context) {
        RideService rideService =
                RetrofitClient.getClient(context).create(RideService.class);

        String startDateParam = startDate.isEmpty() ? null : startDate;
        String endDateParam = endDate.isEmpty() ? null : endDate;
        String emailParam = personEmail.isEmpty() ? null : personEmail;
        String sortParam = sortBy + "," + sortDirection;

        rideService.getAdminRideHistory(
                emailParam,
                startDateParam,
                endDateParam,
                page,
                size,
                sortParam
        ).enqueue(new Callback<>() {

            @Override
            public void onResponse(@NonNull Call<ResponseBody> call,
                                   @NonNull Response<ResponseBody> response) {

                isLoading.setValue(false);

                if (!response.isSuccessful() || response.body() == null) {
                    handleError(response);
                    return;
                }

                parsePagedResponse(response);
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                isLoading.setValue(false);
                errorMessage.setValue("Network error: " + t.getMessage());
                Log.e(TAG, "Admin history failure", t);
            }
        });
    }

    private void parsePagedResponse(Response<ResponseBody> response) {
        try {
            String json = response.body().string();
            JSONObject pageResponse = new JSONObject(json);

            // Extract pagination info
            totalElements.setValue(pageResponse.optInt("totalElements", 0));
            currentPage.setValue(pageResponse.optInt("number", 0));

            // Parse content array
            JSONArray content = pageResponse.getJSONArray("content");
            List<Ride> rideList = new ArrayList<>();

            for (int i = 0; i < content.length(); i++) {
                JSONObject o = content.getJSONObject(i);

                Ride ride = new Ride(
                        (int) o.optLong("id"),
                        o.optString("pickupAddress", "") + " → " +
                                o.optString("destinationAddress", ""),
                        formatDateTime(o.optString("createdAt", "")),
                        "", // endDate not in response
                        o.has("price") && !o.isNull("price")
                                ? o.getDouble("price") : 0.0,
                        o.optString("status", ""),
                        "", "", "", "", "", // driver/vehicle info not in response
                        0.0, // distance not in response
                        "",
                        null
                );

                rideList.add(ride);
            }

            rides.setValue(rideList);

        } catch (Exception e) {
            Log.e(TAG, "JSON parse error", e);
            errorMessage.setValue("Error parsing rides");
        }
    }

    private String formatDateTime(String dateTime) {
        // Format: "2025-02-19T14:30:00" -> "2025-02-19"
        if (dateTime != null && dateTime.length() >= 10) {
            return dateTime.substring(0, 10);
        }
        return "";
    }

    private void handleError(Response<?> response) {
        try {
            String body = response.errorBody() != null
                    ? response.errorBody().string() : "";
            Log.e(TAG, "Error " + response.code() + ": " + body);
            errorMessage.setValue("Error loading rides: " + response.code());
        } catch (Exception e) {
            errorMessage.setValue("Error loading rides");
            e.printStackTrace();
        }
    }
}