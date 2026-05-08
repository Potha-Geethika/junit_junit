package com.carbo.pad.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.Date;

@Document(collection = "pads")
@CompoundIndex(def = "{'organizationId': 1, 'name': 1}", name = "organization_name_index", unique = true)
public class Pad {
    @Id
    private String id;

    @Field("name")
    @NotEmpty(message = "name can not be empty")
    @Size(max = 100, message = "name can not be more than 100 characters.")
    String name;

    @Field("timezone")
    String timezone;

    @Field("created")
    private Long created = new Date().getTime();

    @Field("modified")
    private Long modified  = new Date().getTime();

    @Field("ts")
    private Long ts;

    @Field("operatorId")
    @NotEmpty(message = "operator ID can not be empty")
    @Size(max = 100, message = "operator ID can not be more than 100 characters.")
    private String operatorId;

    @Field("organizationId")
    private String organizationId;

    @Field("serviceCompanyId")
    private String serviceCompanyId;

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

    public Long getTs() {
        return ts;
    }

    public void setTs(Long ts) {
        this.ts = ts;
    }

    public String getOperatorId() {
        return operatorId;
    }

    public void setOperatorId(String operatorId) {
        this.operatorId = operatorId;
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

    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }

    public String getServiceCompanyId() {
        return serviceCompanyId;
    }

    public void setServiceCompanyId(String serviceCompanyId) {
        this.serviceCompanyId = serviceCompanyId;
    }
}
