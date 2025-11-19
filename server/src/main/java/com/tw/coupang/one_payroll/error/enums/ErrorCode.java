package com.tw.coupang.one_payroll.error.enums;

import lombok.Getter;

@Getter
public enum ErrorCode
{

    MISSING_MANDATORY_FIELD("Missing mandatory fields."),
    INVALID_EMAIL("Email format is invalid."),
    INVALID_DATE("Date cannot be null or in the future."),
    NEGATIVE_WORKING_HOURS("Negative working hours detected."),
    MISSING_PAY_PERIOD("Pay period start or end date cannot be null"),
    NEGATIVE_VALUE("Negative value detected for the field: ."),
    INVALID_VALUE("Invalid value provided for the field :");

    private final String errorMessage;

    ErrorCode(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
