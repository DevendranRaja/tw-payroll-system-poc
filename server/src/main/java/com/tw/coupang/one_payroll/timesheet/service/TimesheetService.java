package com.tw.coupang.one_payroll.timesheet.service;

import com.tw.coupang.one_payroll.timesheet.dto.TimesheetRequest;
import com.tw.coupang.one_payroll.timesheet.dto.TimesheetResponse;
import com.tw.coupang.one_payroll.timesheet.entity.TimesheetSummary;
import com.tw.coupang.one_payroll.timesheet.helper.TimesheetValidator;
import com.tw.coupang.one_payroll.timesheet.repository.TimesheetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TimesheetService {

    private final TimesheetRepository timesheetRepository;
    private final TimesheetValidator timesheetValidator;

    @Transactional
    public TimesheetResponse addOrUpdateTimesheet(TimesheetRequest request) {
        //Run Validations
        timesheetValidator.validateRequest(request);

        //Check for existing record
        Optional<TimesheetSummary> existingEntry = timesheetRepository
                .findByEmployeeIdAndPayPeriodId(request.getEmployeeId(), request.getPayPeriodId());

        TimesheetSummary timesheet;
        String operationMessage;
        BigDecimal holidayHrs = request.getHolidayHours() != null ? request.getHolidayHours() : BigDecimal.ZERO;

        if (existingEntry.isPresent()) {
            // UPDATE
            timesheet = existingEntry.get();
            timesheet.setNoOfDaysWorked(request.getNoOfDaysWorked());
            timesheet.setHoursWorked(request.getHoursWorked());
            timesheet.setHolidayHours(holidayHrs);
            operationMessage = "Timesheet updated successfully";
        } else {
            // CREATE
            timesheet = TimesheetSummary.builder()
                    .employeeId(request.getEmployeeId())
                    .payPeriodId(request.getPayPeriodId())
                    .noOfDaysWorked(request.getNoOfDaysWorked())
                    .hoursWorked(request.getHoursWorked())
                    .holidayHours(holidayHrs)
                    .build();
            operationMessage = "Timesheet created successfully";
        }

        //Save into db
        TimesheetSummary saved = timesheetRepository.save(timesheet);

        return mapToResponse(saved, operationMessage);
    }

    private TimesheetResponse mapToResponse(TimesheetSummary entity, String msg) {
        return TimesheetResponse.builder()
                .id(entity.getId())
                .employeeId(entity.getEmployeeId())
                .payPeriodId(entity.getPayPeriodId())
                .hoursWorked(entity.getHoursWorked())
                .noOfDaysWorked(entity.getNoOfDaysWorked())
                .holidayHours(entity.getHolidayHours())
                .updatedAt(entity.getUpdatedAt())
                .message(msg)
                .build();
    }
}
