package com.carbo.admin.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import jakarta.validation.constraints.NotEmpty;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Getter
@Setter
@Document(collection = "users")
public class User {

    @Id
    private String id;

    @Field("firstName")
    @NotEmpty(message = "first name can not be empty")
    private String firstName;

    @Field("lastName")
    @NotEmpty(message = "last name can not be empty")
    private String lastName;

    @Field("userName")
    @Indexed(unique=true, sparse=true)
    @NotEmpty(message = "user name can not be empty")
    private String userName;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Field("password")
    @NotEmpty(message = "password can not be empty")
    private String password;

    @Field("title")
    @NotEmpty(message = "user name can not be empty")
    private String title;

    @Field("authorities")
    private List<Role> authorities;

    @Field("organizationId")
    @NotEmpty(message = "organization id can not be empty")
    private String organizationId;

    @Field("districtId")
    private String districtId;

    @Field("signature")
    private String signature;

    @Field("lastPassResetDate")
    private Date lastPassResetDate;

    @Field("isStrength")
    private boolean isStrength;

    @Field("created")
    private Long created;

    @Field("createdBy")
    private String createdBy;

    @Field("modified")
    private Long modified;

    @Field("lastModifiedBy")
    private String lastModifiedBy;

    @Field("status")
    private String status;
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Field("lastFivePasswords")
    List<String> lastFivePasswords;

    @Field("districtids")
    private List<String> districtids;

    @Field("primaryPhoneNumber")
    private Long primaryPhoneNumber;

    @Field("tableState")
    private Map<String, Object> tableState;

    @JsonProperty("isServiceAccount")
    @Field("isServiceAccount")
    private boolean isServiceAccount;

    @Field("authenticationTime")
    private String authenticationTime;

    @Field("otpCode")
    private String otpCode;

    @Field("otpGeneratedTime")
    private String otpGeneratedTime;

    @Field("firstTimeLogin")
    private boolean firstTimeLogin;

    @Field("fileName")
    private Map<String,Long> fileName;

    @Field("azureId")
    private String azureId;

    @Field("lastLogInTime")
    private long lastLogInTime;

    @Field("selectedColumns")
    private List<SelectedColumns> selectedColumns;

    @Field("savedFilters")
    private Map<String, Object> savedFilters;
}


