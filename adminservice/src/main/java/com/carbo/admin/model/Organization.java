package com.carbo.admin.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.index.Indexed;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Document(collection = "organizations")
public class Organization {
    @Id
    private String id;

    @Field("name")
    @NotEmpty(message = "name can not be empty")
    @Size(max = 100, message = "name can not be more than 100 characters.")
    private String name;

    @Field("domain")
    @NotEmpty(message = "Domain name cannot be empty")
    private Set<String> domains;

    @Field("logoId")
    private String logoId;

    @Field("logoFileName")
    private String logoFileName;

    @Field("aiServiceCompanyName")
    private String aiServiceCompanyName;

    @Field("fieldTicketToggle")
    private Boolean fieldTicketToggle;


//    @Field("type")
//    private OrganizationType type;

    @Field("access")
    Map<Role,Boolean>access;

    @Field("created")
    private Long created = new Date().getTime();

    @Field("modified")
    private Long modified  = new Date().getTime();

    public Map<Role, Boolean> getAccess() {
        return access;
    }

    public void setAccess(Map<Role, Boolean> access) {
        this.access = access;
    }

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

    public String getLogoId() {
        return logoId;
    }

    public void setLogoId(String logoId) {
        this.logoId = logoId;
    }

    public String getLogoFileName() {
        return logoFileName;
    }

    public void setLogoFileName(String logoFileName) {
        this.logoFileName = logoFileName;
    }

    public Set<String> getDomains() {
        return domains;
    }

    public void setDomains(Set<String> domains) {
        this.domains = domains;
    }

    public String getAiServiceCompanyName() {
        return aiServiceCompanyName;
    }

    public void setAiServiceCompanyName(String aiServiceCompanyName) {
        this.aiServiceCompanyName = aiServiceCompanyName;
    }

//    public OrganizationType getType() {
//        return type;
//    }
//
//    public void setType(OrganizationType type) {
//        this.type = type;
//    }

    public Boolean getFieldTicketToggle() {
        return fieldTicketToggle;
    }

    public void setFieldTicketToggle(Boolean fieldTicketToggle) {
        this.fieldTicketToggle = fieldTicketToggle;
    }
}
