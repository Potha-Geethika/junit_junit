package com.carbo.checklist.model;

import org.springframework.data.mongodb.core.mapping.Field;

import java.util.Date;

public class CheckItem {
    @Field("name")
    private String name;


    @Field("description")
    private String description;

    @Field("completed")
    private Boolean completed;

    @Field("modified")
    private Long modified = new Date().getTime();

    @Field("ts")
    private Long ts;

    @Field("lastModifiedBy")
    private String lastModifiedBy;


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getCompleted() {
        return completed;
    }

    public void setCompleted(Boolean completed) {
        this.completed = completed;
    }

}
