package com.tw.coupang.one_payroll.error.validation;

import com.tw.coupang.one_payroll.error.response.ErrorResponse;
import lombok.Getter;

@Getter
public class ValidationException extends RuntimeException {

    private final ErrorResponse errorResponse;

    public ValidationException(ErrorResponse response) {
        super(response.errorMessage());
        this.errorResponse = response;
    }
}
