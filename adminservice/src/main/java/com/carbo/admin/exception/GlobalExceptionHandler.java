package com.carbo.admin.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpClientErrorException;
import com.carbo.admin.model.Error;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(HttpClientErrorException.class)
    public ResponseEntity<Object> httpClientErrorException(HttpClientErrorException ex) {
        log.error("Exception occurred: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Exception occurred due to " + ex.getMessage());
    }

    @ExceptionHandler(ErrorException.class)
    public ResponseEntity<Object> customExceptionHandler(ErrorException ex) {
        log.error("Exception occurred: {}", ex.getMessage());
        return ResponseEntity.status(ex.getError().getHttpStatus()).contentType(MediaType.APPLICATION_JSON).body(ex.getError());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errorMap = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error -> {
            errorMap.put(error.getField(), error.getDefaultMessage());
        });
        log.error("Exception occurred: {}", ex.getMessage());
        Error error = Error.builder().errorCode(ex.getClass().getName()).errorMessage(errorMap).httpStatus(HttpStatus.BAD_REQUEST)
                .build();
        return ResponseEntity.status(error.getHttpStatus()).contentType(MediaType.APPLICATION_JSON).body(error);

    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> exception(Exception ex) {
        log.error("Exception occurred: {}", ex.getMessage());
        Error error = Error.builder().errorCode(ex.getClass().getName()).errorMessage(ex.getMessage()).httpStatus(HttpStatus.INTERNAL_SERVER_ERROR)
                .build();
        return ResponseEntity.status(error.getHttpStatus()).contentType(MediaType.APPLICATION_JSON).body(error);
    }

}
