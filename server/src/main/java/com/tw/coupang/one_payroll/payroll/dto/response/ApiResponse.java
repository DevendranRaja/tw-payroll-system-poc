package com.tw.coupang.one_payroll.payroll.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@AllArgsConstructor
@Builder
@Getter
public class ApiResponse {

    private String code;
    private String message;
    private LocalDateTime timestamp;
    private Object details;

    public static ApiResponse success(String code, String message, Object details) {
        return new ApiResponse(code, message, LocalDateTime.now(), details);
    }

    public static ApiResponse failure(String code, String msg, Object details) {
        return new ApiResponse(code, msg, LocalDateTime.now(), details);
    }
}
