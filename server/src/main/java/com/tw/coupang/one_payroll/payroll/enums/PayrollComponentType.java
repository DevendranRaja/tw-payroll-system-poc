package com.tw.coupang.one_payroll.payroll.enums;

import lombok.Getter;

@Getter
public enum PayrollComponentType {

    INCOME_TAX("Income Tax"),
    PROVIDENT_FUND("Provident Fund"),
    PROFESSIONAL_TAX("Professional Tax"),
    BASIC_SALARY("Basic Salary"),
    HRA("HRA"),
    BONUS("Bonus");

    private final String value;

    PayrollComponentType(String value) {
        this.value = value;
    }

}

