package com.tricolori.backend.dto.report;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter 
@Setter 
@NoArgsConstructor 
@AllArgsConstructor
public class ReportResponse {
    private Long totalCount;
    private Double totalDistance;
    private Double totalMoney;
    private Double averageCount;
    private Double averageDistance;
    private Double averageMoney;
    private List<DailyStatisticDTO> dailyStatistics;
}
