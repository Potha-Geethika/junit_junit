package com.carbo.pad.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.Date;
import java.util.List;

@Data
@Document(collection = "emails")
public class EndStageEmailPayload {
    @Id
    protected String id;

    @Field("emailId")
    @Indexed(unique = false)
    protected String emailId;

    @Field("jobId")
    @Indexed(unique = false)
    private String jobId;

    @Field("well")
    private String well;

    @Field("stage")
    private String stage;

    @Field("pad")
    private String pad;

    @Field("startTime")
    private String startTime;

    @Field("finishTime")
    private String finishTime;

    @Field("pumpStart")
    private Integer pumpStart;

    @Field("pumpEnd")
    private Integer pumpEnd;

    @Field("fieldCoordinator")
    private String fieldCoordinator;

    @Field("serviceSupervisor")
    private String serviceSupervisor;

    @Field("targetStagesPerDay")
    private Integer targetStagesPerDay;

    @Field("actualStagesPerDay")
    private Integer actualStagesPerDay;

    @Field("averagePressure")
    private Float averagePressure;

    @Field("averageRate")
    private Float averageRate;

    @Field("totalCleanFluid")
    private Integer totalCleanFluid;

    @Field("blender")
    private String blender;

    @Field("blender2")
    private String blender2;

    @Field("additionalComments")
    private String additionalComments;

    @Field("formationName")
    private String formationName;

    @Field("pumpsOnGas")
    private Float pumpsOnGas;

    @Field("diesel")
    private Float diesel;

    @Field("fieldGas")
    private Float fieldGas;

    @Field("cng")
    private Float cng;

    @Field("lng")
    private Float lng=0.0f;

    @Field("btu")
    private float btu;

    @Field("blender2PropVolume")
    private double blender2PropVolume;

    @Field("organizationId")
    @Indexed(unique = false)
    private String organizationId;

    @Field("type")
    private EmailType type;

    @Field("created")
    private Long created = new Date().getTime();

    @Field("modified")
    private Long modified  = new Date().getTime();
}
