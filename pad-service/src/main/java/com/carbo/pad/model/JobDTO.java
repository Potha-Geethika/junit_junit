package com.carbo.pad.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
public class JobDTO {
   @JsonProperty("id")
    private String id;

    @JsonProperty("jobNumber")
    private String jobNumber;

    @JsonProperty("pad")
    private String pad;

    @Field("wells")
    private List<Well> wells = new ArrayList<>();

    @Field("startDate")
    private Long startDate;

    @Field("endDate")
    private Long endDate;

    @Field("status")
    private String status;

    @JsonProperty("organizationId")
    private String organizationId;

    @JsonProperty("sharedWithOrganizationId")
    private String sharedWithOrganizationId;

}
