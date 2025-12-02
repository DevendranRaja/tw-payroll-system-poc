package com.tw.coupang.one_payroll.common.utils;

import java.math.BigDecimal;

import static java.math.RoundingMode.HALF_UP;

public class MathsUtils {

    private MathsUtils() {
        // private constructor to prevent instantiation
    }

    public static BigDecimal percentOf(final BigDecimal percent, final BigDecimal amount) {
        if (percent == null) return BigDecimal.ZERO;
        return amount.multiply(percent).divide(BigDecimal.valueOf(100), 2, HALF_UP);
    }
}
