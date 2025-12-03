package com.tw.coupang.one_payroll.common.constants;

public final class SecurityConstants {

    private SecurityConstants() {
    }

    // Roles
    public static final String ROLE_ADMIN = "ADMIN";
    public static final String ROLE_EMPLOYEE = "EMPLOYEE";

    // JWT
    public static final String TOKEN_PREFIX = "Bearer ";
    public static final String AUTHORIZATION = "Authorization";
//    public static final long EXPIRATION_TIME = 86400000; // 24 hours in milliseconds
//


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