package com.tw.coupang.one_payroll.payroll.entity;

import com.tw.coupang.one_payroll.payroll.enums.PayrollStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static jakarta.persistence.EnumType.STRING;
import static jakarta.persistence.GenerationType.IDENTITY;
import static java.math.RoundingMode.HALF_UP;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Entity
@Table(
        name = "payroll_run",
        indexes = {
                @Index(name = "idx_payroll_emp", columnList = "employee_id"),
                @Index(name = "idx_payroll_period", columnList = "pay_period_start, pay_period_end")
        }
)
public class PayrollRun {

    @Id
    @GeneratedValue(strategy =IDENTITY)
    @Column(name = "payroll_id", nullable = false, updatable = false)
    private Long payrollId;

    @NotBlank(message = "Employee ID cannot be empty")
    @Column(name = "employee_id", nullable = false, length = 10)
    private String employeeId;

    @NotNull
    @Column(name = "pay_period_start", nullable = false)
    private LocalDate payPeriodStart;

    @NotNull
    @Column(name = "pay_period_end", nullable = false)
    private LocalDate payPeriodEnd;

    @DecimalMin(value = "0.00", inclusive = true, message = "Gross pay must be positive")
    @Column(name = "gross_pay", precision = 10, scale = 2)
    private BigDecimal grossPay;

    @DecimalMin(value = "0.00", inclusive = true, message = "Tax deduction must not be negative")
    @Column(name = "tax_deduction", precision = 10, scale = 2)
    private BigDecimal taxDeduction;

    @DecimalMin(value = "0.00", inclusive = true, message = "Benefit addition must not be negative")
    @Column(name = "benefit_addition", precision = 10, scale = 2)
    private BigDecimal benefitAddition;

    @DecimalMin(value = "0.00", inclusive = true, message = "Net pay must not be negative")
    @Column(name = "net_pay", precision = 10, scale = 2)
    private BigDecimal netPay;

    @NotNull
    @Enumerated(STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    @Column(name = "status", columnDefinition = "payroll_status DEFAULT 'PROCESSED'")
    private PayrollStatus status;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    @PreUpdate
    public void validatePayroll() {

        if (payPeriodEnd.isBefore(payPeriodStart)) {
            throw new IllegalArgumentException("Pay period end date cannot be before start date");
        }

        if (grossPay != null && netPay != null) {
            grossPay = scale(grossPay);
            taxDeduction = scale(taxDeduction);
            benefitAddition = scale(benefitAddition);
            netPay = scale(netPay);
        }

        if (status == null) {
            status = PayrollStatus.PROCESSED;
        }
    }

    private BigDecimal scale(final BigDecimal value) {
        return value == null ? null : value.setScale(2, HALF_UP);
    }
}
