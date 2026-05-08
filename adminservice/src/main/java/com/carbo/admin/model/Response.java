package com.carbo.admin.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class Response {

    private String successCode;

    private String successMessage;


}
