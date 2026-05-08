package com.carbo.pad.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import jakarta.validation.constraints.NotEmpty;
import java.util.Date;
import java.util.List;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Comparator;

import static com.carbo.pad.utils.ActivityLogUtil.convertToLocalDateTime;

@Data
@Document(collection = "activity-log-entries")
@CompoundIndex(name = "unique_organizationid_jobid_day_well_start_end__stage_index", def = "{'organizationId': 1, 'jobId': 1, 'day': 1, 'well': 1, 'start': 1, 'end': 1, 'stage': 1}", unique = true)
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

    @Override
    public int compareTo(ActivityLogEntry o) {
        return Comparator.comparing(ActivityLogEntry::getDay)
                .thenComparing(ActivityLogEntry::getStart)
                .compare(this, o);
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