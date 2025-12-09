package com.tw.coupang.one_payroll.userauth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class AuthResponse {
    private String userId;
    private String role;
    private String token;
}
