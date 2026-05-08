package com.carbo.pad.model;

import lombok.Data;

@Data
public class ChemicalValue
{
    private Double value;
    private String uom;
    public ChemicalValue(Double value, String uom)
    {
        this.value = value;
        this.uom = uom;
    }
}