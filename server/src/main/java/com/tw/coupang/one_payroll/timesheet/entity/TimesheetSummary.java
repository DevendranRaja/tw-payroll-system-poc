package com.tw.coupang.one_payroll.timesheet.entity;

import jakarta.persistence.*;
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

    @Column(name = "no_of_days_worked")
    private Integer noOfDaysWorked;

    @Column(name = "hours_worked", precision = 6, scale = 2)
    private BigDecimal hoursWorked;

    @Column(name = "holiday_hours", precision = 6, scale = 2)
    @Builder.Default
    private BigDecimal holidayHours = BigDecimal.ZERO;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
