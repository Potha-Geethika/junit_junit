package com.carbo.pad.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.Date;

public class ChemicalUsed {
    @Id
    private String id;

    @Field("date")
    private Date date;

    @Field("submittedAmount")
    private Float submittedAmount;

    @Field("well")
    private String well;

    @Field("stage")
    private Float stage;

    @Field("created")
    private Long created = new Date().getTime();

    @Field("modified")
    private Long modified  = new Date().getTime();

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

    public Float getSubmittedAmount() {
        return submittedAmount;
    }

    public void setSubmittedAmount(Float submittedAmount) {
        this.submittedAmount = submittedAmount;
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
}
