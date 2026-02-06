package com.example.mobile.dto.pricelist;

public class PriceConfigResponse {
    private Double standardPrice;
    private Double luxuryPrice;
    private Double vanPrice;
    private Double kmPrice;
    private String createdAt; // LocalDateTime from backend is serialized as String

    public PriceConfigResponse() {
    }

    public PriceConfigResponse(Double standardPrice, Double luxuryPrice,
                               Double vanPrice, Double kmPrice, String createdAt) {
        this.standardPrice = standardPrice;
        this.luxuryPrice = luxuryPrice;
        this.vanPrice = vanPrice;
        this.kmPrice = kmPrice;
        this.createdAt = createdAt;
    }

    public Double getStandardPrice() {
        return standardPrice;
    }

    public void setStandardPrice(Double standardPrice) {
        this.standardPrice = standardPrice;
    }

    public Double getLuxuryPrice() {
        return luxuryPrice;
    }

    public void setLuxuryPrice(Double luxuryPrice) {
        this.luxuryPrice = luxuryPrice;
    }

    public Double getVanPrice() {
        return vanPrice;
    }

    public void setVanPrice(Double vanPrice) {
        this.vanPrice = vanPrice;
    }

    public Double getKmPrice() {
        return kmPrice;
    }

    public void setKmPrice(Double kmPrice) {
        this.kmPrice = kmPrice;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}