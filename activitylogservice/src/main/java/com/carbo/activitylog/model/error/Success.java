package com.carbo.activitylog.model.error;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;
@Getter
@Setter
@Builder
public class Success {

    private String code;

    private String message;

}
