package com.carbo.pad.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WellDetails {
    private String wellId;
    private String name;
    private String api;
    private int totalStages;
}
