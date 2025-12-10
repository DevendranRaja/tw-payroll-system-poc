package com.tw.coupang.one_payroll.timesheet.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "timesheet_summary",
        uniqueConstraints = @UniqueConstraint(columnNames = {"employee_id", "pay_period_id"}))
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TimesheetSummary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "employee_id", nullable = false)
    private String employeeId;

    @Column(name = "pay_period_id", nullable = false)
    private Integer payPeriodId;

    @Min(0)
    @Column(name = "no_of_days_worked")
    private Integer noOfDaysWorked;

    @DecimalMin("0.00")
    @Column(name = "hours_worked", precision = 6, scale = 2)
    private BigDecimal hoursWorked;

    @DecimalMin("0.00")
    @Builder.Default
    @Column(name = "holiday_hours_worked", precision = 6, scale = 2)
    private BigDecimal holidayHoursWorked = BigDecimal.ZERO;

    @Min(0)
    @Builder.Default
    @Column(name = "holiday_days")
    private Integer holidayDays = 0;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
