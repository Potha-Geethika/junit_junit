package com.carbo.activitylog.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;

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
    @NotEmpty(message = "user name can not be empty")
    private String userName;

    @JsonIgnore
    @Field("password")
    @NotEmpty(message = "password can not be empty")
    private String password;

    @Field("title")
    @NotEmpty(message = "title can not be empty")
    private String title;

    @Field("authorities")
    private List<Role> authorities;

    @Field("organizationId")
    @NotEmpty(message = "organization id can not be empty")
    private String organizationId;

    @Field("azureId")
    private String azureId;

    @Field("districtId")
    private String districtId;

    @Field("signature")
    private String signature;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public List<Role> getAuthorities() {
        return authorities;
    }

    public void setAuthorities(List<Role> authorities) {
        this.authorities = authorities;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(String organizationId) {
        this.organizationId = organizationId;
    }

    public String getDistrictId() {
        return districtId;
    }

    public void setDistrictId(String districtId) {
        this.districtId = districtId;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public String getAzureId() {
        return azureId;
    }

    public void setAzureId(String azureId) {
        this.azureId = azureId;
    }
}
