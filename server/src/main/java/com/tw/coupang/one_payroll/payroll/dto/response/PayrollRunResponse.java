package com.tw.coupang.one_payroll.payroll.dto.response;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;

@Builder
public record PayrollRunResponse(String employeeId, Integer payGroupId, LocalDate payPeriodStart, LocalDate payPeriodEnd,
                                 BigDecimal grossPay, BigDecimal netPay, BigDecimal holidayPay, BigDecimal taxAmount,
                                 BigDecimal benefitsAmount, BigDecimal deductionsAmount, BigDecimal proratedAmount) {

}
