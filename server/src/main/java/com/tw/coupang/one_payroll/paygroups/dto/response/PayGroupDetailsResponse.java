package com.tw.coupang.one_payroll.paygroups.dto.response;

import com.tw.coupang.one_payroll.paygroups.enums.PaymentCycle;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class PayGroupDetailsResponse {

    private Integer payGroupId;
    private String groupName;
    private PaymentCycle paymentCycle;
    private BigDecimal baseTaxRate;
    private BigDecimal benefitRate;
    private BigDecimal deductionRate;
    private LocalDateTime createdAt;
}
