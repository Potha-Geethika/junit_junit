package com.carbo.activitylog.model;

import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Field;
import java.util.Date;

@Document(collection = "deleted-activity-log-entries")
public class DeletedActivityLogEntry {
    @Id
    private String id;

    @Field("day")
    private Integer day;

    @Field("jobId")
    @Indexed (unique = false)
    private String jobId;

    @Field("well")
    private String well;

    @Field("deletedByAPI")
    private Boolean deletedByAPI;

    @Field("deletedByUser")
    private String deletedByUser;

    @Field("stage")
    private Float stage;

    @Field("created")
    private Long created = new Date().getTime();

    @Field("modified")
    private Long modified=new Date().getTime();

    @Field("organizationId")
    @Indexed(unique = false)
    private String organizationId;

    public DeletedActivityLogEntry() {
    }

    public DeletedActivityLogEntry(ActivityLogEntry entry) {
        this.id = entry.getId();
        this.day = entry.getDay();
        this.jobId = entry.getJobId();
        this.well = entry.getWell();
        this.stage = entry.getStage();
        this.created=entry.getCreated();
        this.updateModified();
        this.organizationId = entry.getOrganizationId();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Integer getDay() {
        return day;
    }

    public void setDay(Integer day) {
        this.day = day;
    }

    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
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

    private Long getCreated() {
        return created;
    }

    private void setCreated(Long created) {
        this.created = created;
    }

    public Long getModified() {
        return modified;
    }

    public void updateModified() {
        this.modified = new Date().getTime();
    }

    public String getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(String organizationId) {
        this.organizationId = organizationId;
}

    public Boolean getDeletedByAPI() {
        return deletedByAPI;
    }

    public void setDeletedByAPI(Boolean deletedByAPI) {
        this.deletedByAPI = deletedByAPI;
    }

    public String getDeletedByUser() {
        return deletedByUser;
    }

    public void setDeletedByUser(String deletedByUser) {
        this.deletedByUser = deletedByUser;
    }
}

