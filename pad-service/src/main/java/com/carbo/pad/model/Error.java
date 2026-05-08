package com.carbo.pad.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

@Getter
@Setter
@Builder
public class Error {

    private String errorCode;

    private String errorMessage;

    private HttpStatus httpStatus;

}
