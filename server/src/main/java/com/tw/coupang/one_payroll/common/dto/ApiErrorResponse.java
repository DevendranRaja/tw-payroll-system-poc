package com.tw.coupang.one_payroll.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@AllArgsConstructor
@Builder
@Getter
public class ApiErrorResponse {

    private String code;
    private String message;
    private LocalDateTime timestamp;
    private Object details;

    public static ApiErrorResponse success(String code, String message, Object details) {
        return new ApiErrorResponse(code, message, LocalDateTime.now(), details);
    }

    public static ApiErrorResponse failure(String code, String msg, Object details) {
        return new ApiErrorResponse(code, msg, LocalDateTime.now(), details);
    }
}