package com.carbo.activitylog.model;

import lombok.Data;

@Data
public class StagePerDay {
    int day;
    int completedStagesPerDay;
    int targetStagesPerDay;
    String date;
}
