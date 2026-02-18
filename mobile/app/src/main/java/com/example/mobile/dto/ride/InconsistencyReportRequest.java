package com.example.mobile.dto.ride;

public class InconsistencyReportRequest {

    private String description;

    public InconsistencyReportRequest() {}

    public InconsistencyReportRequest(String description) {
        this.description = description;
    }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}