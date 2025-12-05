package com.tw.coupang.one_payroll.common.constants;

public final class SecurityConstants {

    private SecurityConstants() {
    }

    // JWT
    public static final String TOKEN_PREFIX = "Bearer ";
    public static final String AUTHORIZATION = "Authorization";

    public static final String[] SWAGGER_WHITELIST = {
            "/swagger-ui/**",
            "/v3/api-docs/**"
    };

    public static final String[] AUTH_WHITELIST = {
            "/auth/**"
    };

    // Admin-only endpoints
    public static final String[] ADMIN_URLS = {
            "/employee/**",
            "/employees/**"
    };

    // Employee endpoints
    public static final String[] EMPLOYEE_URLS = {
            "/payslip-ess/**"
    };
}