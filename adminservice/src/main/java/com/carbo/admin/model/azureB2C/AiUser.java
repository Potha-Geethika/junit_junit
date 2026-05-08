package com.carbo.admin.model.azureB2C;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AiUser {
    @JsonProperty (value = "azureUserId")
    private String azureUserId;

    @JsonProperty (value = "tenantId")
    private int tenantId;

    @JsonProperty (value = "tenantName")
    private String tenantName;

    @JsonProperty (value = "id")
    private int id;

    @JsonProperty (value = "userName")
    private String userName;

    @JsonProperty (value = "name")
    private String name;

    @JsonProperty (value = "surname")
    private String surname;

    @JsonProperty (value = "emailAddress")
    private String emailAddress;

    @JsonProperty (value = "mobileNumber")
    private String mobileNumber;

    @JsonProperty (value = "role")
    private String role;

    @JsonProperty (value = "notificationType")
    private String notificationType;

    @JsonProperty (value = "status")
    private String status;
}