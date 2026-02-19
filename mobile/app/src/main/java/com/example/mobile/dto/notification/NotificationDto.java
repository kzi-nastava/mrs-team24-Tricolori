package com.example.mobile.dto.notification;

import com.google.gson.annotations.SerializedName;

public class NotificationDto {

    @SerializedName("id")
    private long id;

    @SerializedName("email")
    private String email;

    @SerializedName("time")
    private String time;

    @SerializedName("opened")
    private boolean opened;

    @SerializedName("content")
    private String content;

    @SerializedName("type")
    private String type;

    @SerializedName("rideId")
    private Long rideId;

    @SerializedName("actionUrl")
    private String actionUrl;

    @SerializedName("driverName")
    private String driverName;

    @SerializedName("passengerName")
    private String passengerName;

    public NotificationDto() {}

    public long getId() { return id; }
    public String getEmail() { return email; }
    public String getTime() { return time; }
    public boolean isOpened() { return opened; }
    public String getContent() { return content; }
    public String getType() { return type; }
    public Long getRideId() { return rideId; }
    public String getActionUrl() { return actionUrl; }
    public String getDriverName() { return driverName; }
    public String getPassengerName() { return passengerName; }

    public void setOpened(boolean opened) { this.opened = opened; }
}