package com.tw.coupang.one_payroll.payroll.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

import static jakarta.persistence.FetchType.EAGER;
import static jakarta.persistence.FetchType.LAZY;

@Entity
@Table(name = "payroll_deductions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PayrollDeductions {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payroll_deduction_id")
    private Integer payrollDeductionId;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "payroll_id", nullable = false)
    private PayrollRun payrollRun;

    @ManyToOne(fetch = EAGER)
    @JoinColumn(name = "deduction_type_id", nullable = false)
    private DeductionType deductionType;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

}
