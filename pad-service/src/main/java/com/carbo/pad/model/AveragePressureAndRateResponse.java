package com.carbo.pad.model;

import lombok.Data;

@Data
public class AveragePressureAndRateResponse {
    private Float stageNumber;
    private Float averagePressure;
    private Float averageRate;
}
