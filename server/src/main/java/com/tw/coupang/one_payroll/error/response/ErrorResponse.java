package com.tw.coupang.one_payroll.error.response;

import java.time.LocalDateTime;

public record ErrorResponse(String moduleName, String employeeId, String errorMessage, LocalDateTime errorTime)
{
    public ErrorResponse {
        if (errorTime == null) {
            errorTime = LocalDateTime.now();
        }
    }
}