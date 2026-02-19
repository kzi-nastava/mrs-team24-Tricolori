package com.example.mobile.dto.ride;

public class Stop {
    private String   address;
    private Location location;

    public Stop() {}

    public Stop(String address, Location location) {
        this.address  = address;
        this.location = location;
    }

    public String   getAddress()  { return address; }
    public Location getLocation() { return location; }
    public void setAddress(String address)    { this.address  = address; }
    public void setLocation(Location location) { this.location = location; }
}
