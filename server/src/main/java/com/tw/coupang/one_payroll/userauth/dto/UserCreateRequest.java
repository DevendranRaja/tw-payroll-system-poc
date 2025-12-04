package com.tw.coupang.one_payroll.userauth.dto;

import com.tw.coupang.one_payroll.userauth.enums.UserRole;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UserCreateRequest {
    @NotBlank
    private String userId;

    @NotBlank
    private String password;

    @NotBlank
    private UserRole role;
}
