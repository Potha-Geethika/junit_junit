package com.carbo.admin.model.azureB2C;

import lombok.Data;

@Data
public class UserResponseDTO {
    //    private String id;
    private String firstName;

    private String lastName;

    private String userName;

    private String organizationId;

    private String organizationName;

    private String azureId;
}
