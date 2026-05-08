package com.carbo.activitylog.model;

import lombok.Data;

@Data
public class PumpHoursPerDay {

    private int day;
    private String date;
    private String completedPumpHoursPerDay;
    private String targetPumpHoursPerDay;
}
