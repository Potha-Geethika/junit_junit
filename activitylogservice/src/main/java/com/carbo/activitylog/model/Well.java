package com.carbo.activitylog.model;

import org.springframework.data.mongodb.core.mapping.Field;

/**
 * Added: 2026-01-15 - Minimal Well representation used by Activity Log service to
 * derive total stage counts per job. Only fields relevant to stage totals are
 * modeled here; any additional fields present in the underlying Mongo document
 * are ignored.
 */
public class Well {

    @Field("name")
    private String name;

    @Field("totalStages")
    private int totalStages;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getTotalStages() {
        return totalStages;
    }

    public void setTotalStages(int totalStages) {
        this.totalStages = totalStages;
    }
}

