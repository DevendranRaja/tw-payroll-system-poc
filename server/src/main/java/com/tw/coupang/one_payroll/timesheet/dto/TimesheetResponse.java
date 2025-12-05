package com.tw.coupang.one_payroll.timesheet.dto;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class TimesheetResponse {
    private Long id;
    private String employeeId;
    private Integer payPeriodId;
    private Integer noOfDaysWorked;
    private BigDecimal hoursWorked;
    private BigDecimal holidayHours;
    private LocalDateTime updatedAt;
    private String message;
}
