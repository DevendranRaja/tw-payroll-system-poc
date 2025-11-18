package com.tw.coupang.one_payroll.paygroups.entity;

import com.tw.coupang.one_payroll.paygroups.enums.PaymentCycle;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "pay_group")
public class PayGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pay_group_id")
    private Integer id;

    @Column(name = "group_name", nullable = false, length = 50)
    private String groupName;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_cycle", nullable = false)
    private PaymentCycle paymentCycle;

    @Column(name = "base_tax_rate", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal baseTaxRate = BigDecimal.valueOf(10.00);

    @Column(name = "benefit_rate", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal benefitRate = BigDecimal.valueOf(5.00);

    @Column(name = "deduction_rate", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal deductionRate = BigDecimal.valueOf(2.50);

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;
}
