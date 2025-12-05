package com.tw.coupang.one_payroll.timesheet.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class TimesheetRequest {
    @NotNull(message = "Employee ID is required")
    private String employeeId;

    @NotNull(message = "Pay Period ID is required")
    private Integer payPeriodId;

    @NotNull(message = "Number of days worked is required")
    @Min(value = 0, message = "Days worked must be >= 0")
    private Integer noOfDaysWorked;

    @NotNull(message = "Hours worked is required")
    @Min(value = 0, message = "Hours worked must be >= 0")
    private BigDecimal hoursWorked;

    // Optional in request, defaults to 0 as of now
    private BigDecimal holidayHours;
}
