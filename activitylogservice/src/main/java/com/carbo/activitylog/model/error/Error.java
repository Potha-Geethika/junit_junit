package com.carbo.activitylog.model.error;

import lombok.*;
import org.springframework.http.HttpStatus;

@Getter
@Setter
@Builder
public class Error {

    private String errorCode;

    private String errorMessage;

    private HttpStatus httpStatus;


}
