package com.carbo.activitylog.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Document(collection = "jobs")
@CompoundIndex(def = "{'_id': 1, 'users._id': 1}", name = "job_id_user_id_index", unique = true)
public class Job {
    @Id
    private String id;

    @Field("name")
    @NotEmpty(message = "name can not be empty")
    @Size(max = 100, message = "name can not be more than 100 characters.")
    private String name;

    @Field("jobNumber")
    @NotEmpty(message = "jobNumber can not be empty")
    @Size(max = 14, message = "jobNumber can not be more than 14 characters.")
    private String jobNumber;

    @Field("ts")
    private Long ts;

    @Field("startDate")
    private Long startDate;

    @Field("pad")
    private String pad;

    @Field("created")
    private Long created = new Date().getTime();

    @Field("modified")
    private Long modified = new Date().getTime();

    @Field("organizationId")
    @Indexed
    private String organizationId;

    @Field("targetStagesPerDay")
    private int targetStagesPerDay;

    @Field("targetDailyPumpTime")
    private float targetDailyPumpTime;

    @Field("lastModifiedBy")
    private String lastModifiedBy;

    @Field("operationsType")
    private String operationsType;

    @Field("bankCount")
    private BankCountEnum bankCount;

    @Field("sharedWithOrganizationId")
    private String sharedWithOrganizationId;

    // Added: 2026-01-15 - Total number of stages across all wells for this job/pad. This
    // field already exists in the underlying jobs collection and is used by downstream
    // services (e.g. reports) as padStageTotal. Expose it here so Activity Log service
    // can use it when building "Complete x stage of the day (xx/yy)" comments.
    @JsonIgnore
    @Field("padStageTotal")
    private Integer padStageTotal;

    // Added: 2026-01-15 - Minimal well list for Activity Log to derive total stages per job
    // when padStageTotal is not available. Only well name and totalStages are required.
    @Field("wells")
    private List<Well> wells = new ArrayList<>();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getJobNumber() {
        return jobNumber;
    }

    public void setJobNumber(String jobNumber) {
        this.jobNumber = jobNumber;
    }

    public String getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(String organizationId) {
        this.organizationId = organizationId;
    }

    public Long getStartDate() {
        return startDate;
    }

    public String getPad() {
        return pad;
    }

    public String getOperationsType() {return operationsType;}

    public void setOperationsType(String operationsType) {this.operationsType = operationsType;}

    public Long getCreated() {return created;}

    public void setCreated(Long created) {this.created = created;}

    public BankCountEnum getBankCount() {return bankCount;}

    public void setBankCount(BankCountEnum bankCount) {this.bankCount = bankCount;}

    public int getTargetStagesPerDay() {
        return targetStagesPerDay;
    }

    public void setTargetStagesPerDay(int targetStagesPerDay) {
        this.targetStagesPerDay = targetStagesPerDay;
    }

    public void setStartDate(Long startDate) {
        this.startDate = startDate;
    }

    public float getTargetDailyPumpTime() {
        return targetDailyPumpTime;
    }

    public void setTargetDailyPumpTime(float targetDailyPumpTime) {
        this.targetDailyPumpTime = targetDailyPumpTime;
    }

    // Added: 2026-01-15 - Getter/setter for padStageTotal so other services can read the
    // total stage count from the Job document without exposing it via JSON.
    public Integer getPadStageTotal() {
        return padStageTotal;
    }

    public void setPadStageTotal(Integer padStageTotal) {
        this.padStageTotal = padStageTotal;
    }

    public List<Well> getWells() {
        return wells;
    }

    public void setWells(List<Well> wells) {
        this.wells = wells;
    }
}
