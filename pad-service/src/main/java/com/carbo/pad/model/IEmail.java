package com.carbo.pad.model;

public interface IEmail {
    String getId();
    void setJobId(String jobId);
    void setTs(long ts);
    Long getTs();
    void setOrganizationId(String organizationId);
    String getOrganizationId();
    Long getCreated();
    void updateModified();
    void setLastModifiedBy(String fullName);
    String getEmailId();
    void setEmailId(String emailId);
    EmailType getEmailType();
    void setMigrated(Boolean value);
}
