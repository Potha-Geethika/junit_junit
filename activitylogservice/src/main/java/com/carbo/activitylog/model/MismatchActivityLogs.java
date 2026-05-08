package com.carbo.activitylog.model;

public class MismatchActivityLogs {

    private String activityLogId;
    private String parentJobId;
    private String activityLogOrganizationId;
    private String jobOrganizationId;
    private String activityLogCreated;

    public String getActivityLogId() {
        return activityLogId;
    }

    public void setActivityLogId(String activityLogId) {
        this.activityLogId = activityLogId;
    }

    public String getParentJobId() {
        return parentJobId;
    }

    public void setParentJobId(String parentJobId) {
        this.parentJobId = parentJobId;
    }

    public String getActivityLogOrganizationId() {
        return activityLogOrganizationId;
    }

    public void setActivityLogOrganizationId(String activityLogOrganizationId) {
        this.activityLogOrganizationId = activityLogOrganizationId;
    }

    public String getJobOrganizationId() {
        return jobOrganizationId;
    }

    public void setJobOrganizationId(String jobOrganizationId) {
        this.jobOrganizationId = jobOrganizationId;
    }

    public String getActivityLogCreated() {return activityLogCreated;}

    public void setActivityLogCreated(String activityLogCreated) {this.activityLogCreated = activityLogCreated;}
}
