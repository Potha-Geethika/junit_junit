package com.carbo.activitylog.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import jakarta.validation.constraints.NotEmpty;
import java.util.Date;

@Document(collection = "pump-issues")
public class PumpIssue {
    @Id
    private String id;

    @Field("date")
    @NotEmpty(message = "date can not be empty")
    private Date date;

    @Field("shift")
    private String shift;

    @Field("pumpNumber")
    @NotEmpty(message = "pumpNumber can not be empty")
    private String pumpNumber;

    @Field("issue")
    @NotEmpty(message = "issue can not be empty")
    private String issue;

    @Field("hole")
    private Integer hole;

    @Field("jobId")
    private String jobId;

    @Field("well")
    private String well;

    @Field("stage")
    private Float stage;

    @Field("padStage")
    private Integer padStage;

    @Field("created")
    private Long created = new Date().getTime();

    @Field("modified")
    private Long modified  = new Date().getTime();

    @Field("ts")
    private Long ts;

    @Field("organizationId")
    private String organizationId;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getShift() {
        return shift;
    }

    public void setShift(String shift) {
        this.shift = shift;
    }

    public String getPumpNumber() {
        return pumpNumber;
    }

    public void setPumpNumber(String pumpNumber) {
        this.pumpNumber = pumpNumber;
    }

    public String getIssue() {
        return issue;
    }

    public void setIssue(String issue) {
        this.issue = issue;
    }

    public Integer getHole() {
        return hole;
    }

    public void setHole(Integer hole) {
        this.hole = hole;
    }

    public String getWell() {
        return well;
    }

    public void setWell(String well) {
        this.well = well;
    }

    public Float getStage() {
        return stage;
    }

    public void setStage(Float stage) {
        this.stage = stage;
    }

    public Integer getPadStage() {
        return padStage;
    }

    public void setPadStage(Integer padStage) {
        this.padStage = padStage;
    }

    public Long getTs() {
        return ts;
    }

    public void setTs(Long ts) {
        this.ts = ts;
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

    public Long getCreated() {
        return created;
    }
}
