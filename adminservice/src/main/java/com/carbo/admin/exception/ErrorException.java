package com.carbo.admin.exception;

import com.carbo.admin.model.Error;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ErrorException extends RuntimeException{

    private final Error error;

}
