package com.tw.coupang.one_payroll.paygroups.enums;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum PaymentCycle {
    WEEKLY,
    BIWEEKLY,
    MONTHLY;
    @JsonCreator
    public static PaymentCycle fromString(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Invalid payment cycle: empty value");
        }
        return PaymentCycle.valueOf(value.toUpperCase());
    }
}
