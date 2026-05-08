package com.carbo.activitylog.model;

import lombok.Data;

import java.util.List;

@Data
public class PumpTimeHistoryResponse {
    private List<PumpTimeHistoryTable> pumpTimeHistoryTable;
    private double total;
}
