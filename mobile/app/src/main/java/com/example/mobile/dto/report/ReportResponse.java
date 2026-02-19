package com.example.mobile.dto.report;

import java.util.List;

public class ReportResponse {
    private Long   totalCount;
    private Double totalDistance;
    private Double totalMoney;
    private Double averageCount;
    private Double averageDistance;
    private Double averageMoney;
    private List<DailyStatisticDTO> dailyStatistics;

    public Long   getTotalCount()     { return totalCount; }
    public Double getTotalDistance()  { return totalDistance; }
    public Double getTotalMoney()     { return totalMoney; }
    public Double getAverageCount()   { return averageCount; }
    public Double getAverageDistance(){ return averageDistance; }
    public Double getAverageMoney()   { return averageMoney; }
    public List<DailyStatisticDTO> getDailyStatistics() { return dailyStatistics; }
}
