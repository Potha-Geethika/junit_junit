package com.carbo.pad.model;

import lombok.Data;

import java.util.Map;

@Data
public class SummaryChart {
    private Map<String, ChemicalValue> chemicalValues;
    private TotalPropAndClean totalPropAndClean;
}