package com.carbo.activitylog.model;

import lombok.Data;

import java.util.List;

@Data
public class PumpTimeHistoryTable {
    private String date;
    private String well;
    private Float stage;
    private String activity;
    private String NptCode;
    private String duration;
    private List<String> equipment;
    private List<String> issue;
    private String operationNotes;
}
