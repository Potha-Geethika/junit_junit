package com.carbo.activitylog.model;

import lombok.Data;
import lombok.NonNull;

@Data
public class ActivityLogCopyPayload {

    @NonNull
    private String jobId;

    @NonNull
    private Integer fromDay;

    @NonNull
    private Integer toDay;
}
