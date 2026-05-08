package com.carbo.pad.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PadTotals {
    private double totalProppantPumpedLbs;
    private double totalWaterBbls;
    private double averageRateBpm;
    private double averagePressurePsi;
    private double totalPumpHours;
}
