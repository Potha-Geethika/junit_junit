package com.carbo.admin.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;
import java.util.Set;

@Data
public class UserPatchDTO {
    @JsonProperty("userId")
    private String userId;

    @JsonProperty("selectedColumns")
    private List<SelectedColumns> selectedColumns;
}
