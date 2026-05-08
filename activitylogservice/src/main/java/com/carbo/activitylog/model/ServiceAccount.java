package com.carbo.activitylog.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Document(collection = "service-accounts")
public class ServiceAccount {
    @Id
    private String id;

    @Field("userId")
    private String userId;

    @Field("organizationIds")
    private List<String> organizationIds = new ArrayList<>();

    @Field("microservices")
    private List<String> microservices = new ArrayList<>();

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

    public Long getTs() {
        return ts;
    }

    public void setTs(Long ts) {
        this.ts = ts;
    }

    public String getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(String organizationId) {
        this.organizationId = organizationId;
    }

    public List<String> getOrganizationIds() {
        return organizationIds;
    }

    public void setOrganizationIds(List<String> organizationIds) {
        this.organizationIds = organizationIds;
    }

    public List<String> getMicroservices() {
        return microservices;
    }

    public void setMicroservices(List<String> microservices) {
        this.microservices = microservices;
    }

    public Long getCreated() {
        return created;
    }
}
