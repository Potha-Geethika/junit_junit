package com.carbo.pad.model;

import lombok.Data;

@Data
public class WellCompletionInformation {
    private CleanTotals cleanTotals;
    private ProppantPumped proppantPumped;
    private PumpPerformance pumpPerformance;
    private SummaryChart summaryChart;
}
