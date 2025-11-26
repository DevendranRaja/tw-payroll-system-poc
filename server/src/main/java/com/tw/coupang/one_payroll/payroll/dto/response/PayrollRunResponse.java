package com.tw.coupang.one_payroll.payroll.dto.response;

import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record PayrollRunResponse(String employeeId, String payGroupId, String payPeriod, BigDecimal grossPay,
                                 BigDecimal netPay, BigDecimal holidayPay, BigDecimal taxAmount,
                                 BigDecimal benefitsAmount, BigDecimal deductionsAmount, BigDecimal proratedAmount) {

}
