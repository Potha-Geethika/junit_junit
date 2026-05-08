package com.carbo.activitylog.model;

import lombok.Data;

import java.util.List;

@Data
public class ActivityAndNptHistoryResponse {
    private List<PumpTimeHistoryTable> pumpTimeHistoryTable;
    private String total;
}