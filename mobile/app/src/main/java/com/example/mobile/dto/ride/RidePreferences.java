package com.example.mobile.dto.ride;

import com.example.mobile.enums.VehicleType;
import com.google.gson.annotations.SerializedName;

import java.time.LocalDateTime;

public class RidePreferences {
    @SerializedName("vehicleType")
    private VehicleType vehicleType;

    @SerializedName("petFriendly")
    private boolean petFriendly;

    @SerializedName("babyFriendly")
    private boolean babyFriendly;

    @SerializedName("scheduledFor")
    private LocalDateTime scheduledFor;  // null → ride now

    public RidePreferences() {}

    public RidePreferences(VehicleType vehicleType,
                           boolean petFriendly,
                           boolean babyFriendly,
                           LocalDateTime scheduledFor) {
        this.vehicleType  = vehicleType;
        this.petFriendly  = petFriendly;
        this.babyFriendly = babyFriendly;
        this.scheduledFor = scheduledFor;
    }

    public VehicleType    getVehicleType()  { return vehicleType; }
    public boolean        isPetFriendly()   { return petFriendly; }
    public boolean        isBabyFriendly()  { return babyFriendly; }
    public LocalDateTime  getScheduledFor() { return scheduledFor; }
}
