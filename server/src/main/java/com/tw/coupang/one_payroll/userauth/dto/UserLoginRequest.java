package com.tw.coupang.one_payroll.userauth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UserLoginRequest {
    @NotBlank
    private String userId;

    @NotBlank
    private String password;
}

