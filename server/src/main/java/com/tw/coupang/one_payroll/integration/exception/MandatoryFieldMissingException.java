package com.tw.coupang.one_payroll.integration.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class MandatoryFieldMissingException extends RuntimeException {
    public MandatoryFieldMissingException(String fieldName) {
        super(String.format("One mandatory field must be provided: %s is missing.", fieldName));
    }
}