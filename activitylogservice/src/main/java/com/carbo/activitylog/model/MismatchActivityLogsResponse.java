package com.carbo.activitylog.model;

import java.util.List;

public class MismatchActivityLogsResponse {

    private List<MismatchActivityLogs> mismatchActivityLogsList;
    private Integer totalCount;

    public List<MismatchActivityLogs> getMismatchActivityLogsList() {
        return mismatchActivityLogsList;
    }

    public void setMismatchActivityLogsList(List<MismatchActivityLogs> mismatchActivityLogsList) {
        this.mismatchActivityLogsList = mismatchActivityLogsList;
    }

    public Integer getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(Integer totalCount) {
        this.totalCount = totalCount;
    }
}
