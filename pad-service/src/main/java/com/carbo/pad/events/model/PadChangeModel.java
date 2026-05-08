package com.carbo.pad.events.model;

import com.carbo.pad.model.Pad;

public class PadChangeModel {
    private String type;
    private String action;
    private String organizationId;
    private String padName;
    private String updatedTimezone;
    private String previousTimezone;

    public PadChangeModel(String type, String action, Pad updatedPad, String previousTimezone) {
        super();
        this.type = type;
        this.action = action;
        this.organizationId = updatedPad.getOrganizationId();
        this.padName = updatedPad.getName();
        this.updatedTimezone = updatedPad.getTimezone();
        this.previousTimezone = previousTimezone;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }


    public String getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(String organizationId) {
        this.organizationId = organizationId;
    }

    public String getPadName() {
        return padName;
    }

    public void setPadName(String padName) {
        this.padName = padName;
    }

    public String getUpdatedTimezone() {
        return updatedTimezone;
    }

    public void setUpdatedTimezone(String updatedTimezone) {
        this.updatedTimezone = updatedTimezone;
    }

    public String getPreviousTimezone() {
        return previousTimezone;
    }

    public void setPreviousTimezone(String previousTimezone) {
        this.previousTimezone = previousTimezone;
    }
}
