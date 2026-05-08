package com.carbo.pad.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PadDetailsResponse {
    private List<WellDetails> wells;
    private DailyAverages dailyAverages;
    private PadTotals padTotals;
    private double calculatedOverDays;
}
