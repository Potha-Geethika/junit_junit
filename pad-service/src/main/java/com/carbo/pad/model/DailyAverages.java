package com.carbo.pad.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DailyAverages {
    private double overallAverageStages;
    private double averageProppantLbs;    // lbs per day
    private String averagePumpingTimeHrs; // hours per day
    private String averageTotalNPTHrs;    // hours per day
    private String averageServiceNPTHrs;  // hours per day
    private double fracEfficiencyPercentage;
}
