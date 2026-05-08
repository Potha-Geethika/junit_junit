package com.carbo.pad.model;

import lombok.Data;

@Data
public class AverageVsMaxResponse {
    private Float stageNumber;
    private Float averagePressure;
    private Float maxPressure;
    private Float averageRate;
    private Float maxRate;
}
