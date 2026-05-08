package com.carbo.pad.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Document(collection = "wells")
public class Well {
    @Id
    private String id;

    @Field("name")
    @NotEmpty(message = "name can not be empty")
    @Size(max = 100, message = "name can not be more than 100 characters.")
    private String name;

    @Field("api")
    @NotEmpty(message = "api can not be empty")
    @Size(max = 14, message = "api can not be more than 14 characters.")
    private String api;

    @Field("afeNumber")
    @NotEmpty(message = "afeNumber can not be empty")
    @Size(max = 20, message = "afeNumber can not be more than 20 characters.")
    private String afeNumber;

    @Field("longitude")
    private double longitude;

    @Field("latitude")
    private double latitude;

    @Field("proppants")
    private List<Proppant> proppants = new ArrayList<>();

    @Field("totalStages")
    private int totalStages;

    @Field("operatorId")
    @NotEmpty(message = "pad ID can not be empty")
    @Size(max = 100, message = "pad ID can not be more than 100 characters.")
    private String operatorId;

    @Field("padId")
    @NotEmpty(message = "pad ID can not be empty")
    @Size(max = 100, message = "pad ID can not be more than 100 characters.")
    private String padId;

    @Field("fracproId")
    private int fracproId;

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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getApi() {
        return api;
    }

    public void setApi(String api) {
        this.api = api;
    }

    public String getAfeNumber() {
        return afeNumber;
    }

    public void setAfeNumber(String afeNumber) {
        this.afeNumber = afeNumber;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public int getTotalStages() {
        return totalStages;
    }

    public void setTotalStages(int totalStages) {
        this.totalStages = totalStages;
    }

    public Long getTs() {
        return ts;
    }

    public void setTs(Long ts) {
        this.ts = ts;
    }

    public int getFracproId() {
        return fracproId;
    }

    public void setFracproId(int fracproId) {
        this.fracproId = fracproId;
    }

    public String getOperatorId() {
        return operatorId;
    }

    public void setOperatorId(String operatorId) {
        this.operatorId = operatorId;
    }

    public String getPadId() {
        return padId;
    }

    public void setPadId(String padId) {
        this.padId = padId;
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

    public List<Proppant> getProppants() {
        return proppants;
    }

    public void setProppants(List<Proppant> proppants) {
        this.proppants = proppants;
    }
}