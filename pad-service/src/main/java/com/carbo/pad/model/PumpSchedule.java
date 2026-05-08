package com.carbo.pad.model;

import jakarta.validation.constraints.NotEmpty;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.Date;

@Document(collection = "pump-schedules")
@CompoundIndex(def = "{'jobId': 1, 'wellId': 1, 'stageNumber': 1, 'stepNumber': 1}", name = "job_well_stage_step_index", unique = true)
public class PumpSchedule {
    @Id
    private String id;

    @Field("jobId")
    @NotEmpty(message = "job id can not be empty")
    private String jobId;

    @Field("wellId")
    @NotEmpty(message = "well id can not be empty")
    private String wellId;

    @Field("stageNumber")
    @NotEmpty(message = "stage number can not be empty")
    private Float stageNumber;

    @Field("stepNumber")
    @NotEmpty(message = "step number can not be empty")
    private Integer stepNumber;

    @Field("stepName")
    @NotEmpty(message = "step name can not be empty")
    private String stepName;

    @Field("stepLength")
    private String stepLength;

    public String getDesignName() {
        return designName;
    }

    public void setDesignName(String designName) {
        this.designName = designName;
    }

    @Field("fluidType")
    private String fluidType;

    @Field("designId")
    private String designId;

    public String getDesignId() {
        return designId;
    }

    public void setDesignId(String designId) {
        this.designId = designId;
    }

    @Field("designName")
    private String designName;

    @Field("flush")
    private Flush flush;

    @Field("proppantType")
    private String proppantType;

    @Field("proppantConcentration")
    private Float proppantConcentration;

    @Field("proppantConcentrationTo")
    private Float proppantConcentrationTo;

    @Field("rate")
    private Float rate;

    @Field("rate2")
    private double rate2;

    @Field("cleanVol")
    private Float cleanVol;

    @Field("slurryVol")
    private Float slurryVol;

    @Field("created")
    private Long created = new Date().getTime();

    @Field("modified")
    private Long modified = new Date().getTime();

    @Field("organizationId")
    private String organizationId;

    @Field("ts")
    private Long ts;

    @Field("lastModifiedBy")
    private String lastModifiedBy;

    @Field("stageMode")
    private String stageMode;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getStepName() {
        return stepName;
    }

    public void setStepName(String stepName) {
        this.stepName = stepName;
    }

    public String getProppantType() {
        return proppantType;
    }

    public void setProppantType(String proppantType) {
        this.proppantType = proppantType;
    }

    public Float getProppantConcentration() {
        return proppantConcentration;
    }

    public void setProppantConcentration(Float proppantConcentration) {
        this.proppantConcentration = proppantConcentration;
    }

    public Float getRate() {
        return rate;
    }

    public void setRate(Float rate) {
        this.rate = rate;
    }

    public Float getCleanVol() {
        return cleanVol;
    }

    public void setCleanVol(Float cleanVol) {
        this.cleanVol = cleanVol;
    }

    public Float getSlurryVol() {
        return slurryVol;
    }

    public void setSlurryVol(Float slurryVol) {
        this.slurryVol = slurryVol;
    }

    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    public String getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(String organizationId) {
        this.organizationId = organizationId;
    }

    public Long getTs() {
        return ts;
    }

    public void setTs(Long ts) {
        this.ts = ts;
    }

    public void setLastModifiedBy(String lastModifiedBy) {
        this.lastModifiedBy = lastModifiedBy;
    }

    public void updateModified() {
        this.modified = new Date().getTime();
    }

    public Long getCreated() {
        return created;
    }

    public Integer getStepNumber() {
        return stepNumber;
    }

    public void setStepNumber(Integer stepNumber) {
        this.stepNumber = stepNumber;
    }

    public Float getStageNumber() {
        return stageNumber;
    }

    public void setStageNumber(Float stageNumber) {
        this.stageNumber = stageNumber;
    }

    public String getStepLength() {
        return stepLength;
    }

    public void setStepLength(String stepLength) {
        this.stepLength = stepLength;
    }

    public String getWellId() {
        return wellId;
    }

    public void setWellId(String wellId) {
        this.wellId = wellId;
    }

    public Float getProppantConcentrationTo() {
        return proppantConcentrationTo;
    }

    public void setProppantConcentrationTo(Float proppantConcentrationTo) {
        this.proppantConcentrationTo = proppantConcentrationTo;
    }

    public String getFluidType() {
        return fluidType;
    }

    public void setFluidType(String fluidType) {
        this.fluidType = fluidType;
    }

    public Long getModified() {
        return modified;
    }

    public Flush getFlush() {
        return flush;
    }

    public void setFlush(Flush flush) {
        this.flush = flush;
    }

    public double getRate2() {
        return rate2;
    }

    public void setRate2(double rate2) {
        this.rate2 = rate2;
    }

    public String getStageMode() {
        return stageMode;
    }

    public void setStageMode(String stageMode) {
        this.stageMode = stageMode;
    }

}
