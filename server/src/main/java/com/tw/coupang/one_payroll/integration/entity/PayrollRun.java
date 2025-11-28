package com.tw.coupang.one_payroll.integration.entity;

import com.tw.coupang.one_payroll.integration.enums.PayrollStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

//this class can be used to map payroll_run table in the db, it can be removed after the other team creates their own entity
@Entity
@Table(name = "payroll_run")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PayrollRun {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payroll_id")
    private Integer payrollId;

    @Column(name = "employee_id", nullable = false, length = 10)
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

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    // Automatically set timestamp before persisting - Not required as it's handled by DB
    @PrePersist
    protected void onCreate() {
        /*if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }*/
        if (status == null) {
            status = PayrollStatus.PROCESSED; // Default per schema
        }
    }
}
