package com.carbo.activitylog.exception;

import com.carbo.activitylog.model.error.Error;
import lombok.AllArgsConstructor;
import lombok.Getter;


@Getter
@AllArgsConstructor
public class ErrorException extends RuntimeException{
    private final Error error;
}
