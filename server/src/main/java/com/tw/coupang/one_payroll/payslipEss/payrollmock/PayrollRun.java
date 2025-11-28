package com.tw.coupang.one_payroll.payslipEss.payrollmock;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "payroll_run")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PayrollRun {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payroll_id")
    private Integer payrollId;

    @Column(name = "employee_id", length = 10, nullable = false)
    private String employeeId;

    @Column(name = "pay_period_start", nullable = false)
    private LocalDate payPeriodStart;

    @Column(name = "pay_period_end", nullable = false)
    private LocalDate payPeriodEnd;

    @Column(name = "gross_pay", precision = 10, scale = 2)
    private BigDecimal grossPay;

    @Column(name = "tax_deduction", precision = 10, scale = 2)
    private BigDecimal taxDeduction;

    @Column(name = "benefit_addition", precision = 10, scale = 2)
    private BigDecimal benefitAddition;

    @Column(name = "net_pay", precision = 10, scale = 2)
    private BigDecimal netPay;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private PayrollStatus status;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public enum PayrollStatus {
        PROCESSED, FAILED
    }
}
