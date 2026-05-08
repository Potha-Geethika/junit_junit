package com.carbo.admin.model;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public enum SelectedColumns {

    FIRST_NAME("firstName"),
    LAST_NAME("lastName"),
    USER_NAME("userName"),
    TITLE("title"),
    ORGANIZATION_ID("organizationId"),
    DISTRICT_ID("districtId"),
    CREATED("created"),
    CREATED_BY("createdBy"),
    MODIFIED("modified"),
    LAST_MODIFIED_BY("lastModifiedBy"),
    STATUS("status"),
    DISTRICT_IDS("districtids"),
    LAST_LOGIN_TIME("lastLogInTime"),
    ACCESS_LEVEL("accessLevel");

    private final String fieldName;
    SelectedColumns(String fieldName) {
        this.fieldName = fieldName;
    }

    @JsonValue
    public String getFieldName() {
        return fieldName;
    }

}
