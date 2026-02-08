package com.example.mobile.dto.pricelist;

public class PriceConfigRequest {
    private Double standardPrice;
    private Double luxuryPrice;
    private Double vanPrice;
    private Double kmPrice;

    public PriceConfigRequest() {
    }

    public PriceConfigRequest(Double standardPrice, Double luxuryPrice,
                              Double vanPrice, Double kmPrice) {
        this.standardPrice = standardPrice;
        this.luxuryPrice = luxuryPrice;
        this.vanPrice = vanPrice;
        this.kmPrice = kmPrice;
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
}