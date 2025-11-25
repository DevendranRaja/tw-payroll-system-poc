package com.tw.coupang.one_payroll.payroll.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class ApiResponse {

    private String code;
    private String message;
    private LocalDateTime timestamp;
    private Object details;

    public static ApiResponse success(String msg, Object details) {
        return new ApiResponse("SUCCESS", msg, LocalDateTime.now(), details);
    }

    public static ApiResponse error(String code, String msg) {
        return new ApiResponse(code, msg, LocalDateTime.now(), null);
    }
}
