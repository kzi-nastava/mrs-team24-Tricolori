package com.example.mobile.dto.report;

public class DailyStatisticDTO {
    private String date;
    private Long   count;
    private Double distance;
    private Double money;

    /*--- Getters & Setters ---*/
    public String getDate() {
        return date;
    }
    public Long getCount() {
        return count;
    }
    public Double getDistance() {
        return distance;
    }
    public Double getMoney() {
        return money;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setCount(Long count) {
        this.count = count;
    }

    public void setDistance(Double distance) {
        this.distance = distance;
    }
    public void setMoney(Double money) {
        this.money = money;
    }
}
