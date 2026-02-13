package com.tricolori.backend.dto.report;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter 
@NoArgsConstructor 
@AllArgsConstructor
public class DailyStatisticDTO {
    private String date;      // Formatted date (ex. "01.01.") - used for label on X axis
    private Long count;       // Value for "Rides per days" graphic
    private Double distance;  // Value for "Kilometers per days" graphic
    private Double money;     // Value for "Money per days" graphic
}
