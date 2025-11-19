package com.tw.coupang.one_payroll.paygroups.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.Arrays;

public enum PaymentCycle {
    WEEKLY,
    BIWEEKLY,
    MONTHLY;

    @JsonCreator
    public static PaymentCycle fromString(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Invalid payment cycle: value cannot be empty. " +
                    "Valid values are: " + String.join(", ", valuesAsString()));
        }

        try {
            return PaymentCycle.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException(
                    "Invalid payment cycle: " + value + ". Valid values are: " + String.join(", ", valuesAsString())
            );
        }
    }

    private static String[] valuesAsString() {
        return Arrays.stream(PaymentCycle.values())
                .map(Enum::name)
                .toArray(String[]::new);
    }
}
