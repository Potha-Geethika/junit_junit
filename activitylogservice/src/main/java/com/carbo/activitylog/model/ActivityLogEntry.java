package com.carbo.activitylog.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import jakarta.validation.constraints.NotEmpty;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import static com.carbo.activitylog.utils.ActivityLogUtil.convertToLocalDateTime;

@Document(collection = "activity-log-entries")
@CompoundIndex (name = "unique_organizationid_jobid_day_well_start_end__stage_index", def = "{'organizationId': 1, 'jobId': 1, 'day': 1, 'well': 1, 'start': 1, 'end': 1, 'stage': 1}", unique = true)
public class ActivityLogEntry implements Comparable<ActivityLogEntry> {
    @Id
    private String id;

    @Field("date")
    @NotEmpty(message = "date can not be empty")
    private Date date;

    @Field("day")
    private Integer day;

    @Field("jobId")
    @Indexed(unique = false)
    private String jobId;

    @Field("well")
    private String well;

    @Field("stage")
    private Float stage;

    @Field("start")
    private String start;

    @Field("end")
    private String end;

    @Field("opsActivity")
    private String opsActivity;

    @Field("eventOrNptCode")
    private String eventOrNptCode;

    @Field("complete")
    private Boolean complete;

    @Field("completedDate")
    private Long completedDate;

    @Field("subNptCode")
    private String subNptCode;

    @Field("equipment")
    private List<String> equipment;

    @Field("equipmentIssueId")
    private List<String> equipmentIssueId;

    @Field("comments")
    private String comments;

    @Field("created")
    private Long created = new Date().getTime();

    @Field("modified")
    private Long modified = new Date().getTime();

    @Field("lastModifiedBy")
    private String lastModifiedBy;

    @Field("ts")
    private Long ts;

    @Field("organizationId")
    @Indexed(unique = false)
    private String organizationId;

    @Field("endTimeOnNextDay")
    private Boolean endTimeOnNextDay;

    @Field("issue")
    private List<String> issue;

    @Field("bank")
    private String bank;

    @Field("fracProConnectNextDay")
    private boolean fracProConnectNextDay;

    @Field("firstActivityCompletionDate")
    private String firstActivityCompletionDate;

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

    public Integer getDay() {
        return day;
    }

    public void setDay(Integer day) {
        this.day = day;
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

    public String getStart() {
        return start;
    }

    public void setStart(String start) {
        this.start = start;
    }

    public String getEnd() {
        return end;
    }

    public void setEnd(String end) {
        this.end = end;
    }

    public String getOpsActivity() {
        return opsActivity;
    }

    public void setOpsActivity(String opsActivity) {
        this.opsActivity = opsActivity;
    }

    public String getEventOrNptCode() {
        return eventOrNptCode;
    }

    public void setEventOrNptCode(String eventOrNptCode) {
        this.eventOrNptCode = eventOrNptCode;
    }

    public Boolean getComplete() {
        return complete != null && complete;
    }

    public void setComplete(Boolean complete) {
        this.complete = complete;
    }

    public Long getCompletedDate() {
        return completedDate;
    }

    public void setCompletedDate(Long completedDate) {
        this.completedDate = completedDate;
    }

    public String getSubNptCode() {
        return subNptCode;
    }

    public void setSubNptCode(String subNptCode) {
        this.subNptCode = subNptCode;
    }

    public List<String> getEquipment() {
        return equipment;
    }

    public void setEquipment(List<String> equipment) {
        this.equipment = equipment;
    }

    public List<String> getEquipmentIssueId() {
        return equipmentIssueId;
    }

    public void setEquipmentIssueId(List<String> equipmentIssueId) {
        this.equipmentIssueId = equipmentIssueId;
    }

    public List<String> getIssue() {
        return issue;
    }

    public void setIssue(List<String> issue) {
        this.issue = issue;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }



    public Boolean getEndTimeOnNextDay() {
        return endTimeOnNextDay;
    }

    public void setEndTimeOnNextDay(Boolean endTimeOnNextDay) {
        this.endTimeOnNextDay = endTimeOnNextDay;
    }

    @Override
    public int compareTo(ActivityLogEntry o) {
        return Comparator.comparing(ActivityLogEntry::getDay)
                .thenComparing(ActivityLogEntry::getStart)
                .compare(this, o);
    }

    public Long getCreated() {
        return created;
    }

    // Added: 2025-12-23 - Expose modified timestamp so services can derive completion ordering
    // when completedDate is not available (for legacy Activity Log records).
    public Long getModified() {
        return modified;
    }

    public void updateModified() {
        this.modified = new Date().getTime();
    }

    public void setLastModifiedBy(String lastModifiedBy) {
        this.lastModifiedBy = lastModifiedBy;
    }

    public String getBank() {return bank;}

    public void setBank(String bank) {this.bank = bank;}

    public boolean isFracProConnectNextDay() {
        return fracProConnectNextDay;
    }

    public void setFracProConnectNextDay(boolean fracProConnectNextDay) {
        this.fracProConnectNextDay = fracProConnectNextDay;
    }

    public String getFirstActivityCompletionDate() {
        return firstActivityCompletionDate;
    }

    public void setFirstActivityCompletionDate(String firstActivityCompletionDate) {
        this.firstActivityCompletionDate = firstActivityCompletionDate;
    }

    public long getMillisecondsSpan() {
        if (start != null && end != null) {
            Duration dur = getDuration();
            return dur.toMillis();
        } else {
            return 0;
        }
    }

    private Duration getDuration() {
        if (start == null || end == null) {
            return Duration.ZERO;
        } else {
            LocalDateTime startTime = convertToLocalDateTime(start);
            LocalDateTime endTime = convertToLocalDateTime(end);
            if (end.length() == 5 && endTime.toLocalTime() == LocalTime.MIDNIGHT) {
                endTime = endTime.plusDays(1);
            }
            if (startTime.isAfter(endTime)) {
                throw new IllegalStateException("Start time cannot be after end time");
            }
            return Duration.between(startTime, endTime);
        }
    }
}
