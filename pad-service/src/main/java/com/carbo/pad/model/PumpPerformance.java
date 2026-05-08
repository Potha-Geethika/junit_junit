package com.carbo.pad.model;

import lombok.Data;

import java.util.List;

@Data
public class PumpPerformance {

    private List<Float> pumpTime;
    private List<Float> averagePressure;
    private List<Float> maxPressure;
    private List<Float> averageRate;
    private List<Float> maxRate;
}
