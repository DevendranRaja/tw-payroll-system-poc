package com.tw.coupang.one_payroll.timesheet.service;

import com.tw.coupang.one_payroll.timesheet.dto.TimesheetRequest;
import com.tw.coupang.one_payroll.timesheet.dto.TimesheetResponse;
import com.tw.coupang.one_payroll.timesheet.entity.TimesheetSummary;
import com.tw.coupang.one_payroll.timesheet.helper.TimesheetValidator;
import com.tw.coupang.one_payroll.timesheet.repository.TimesheetRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TimesheetServiceTest {

    @Mock
    private TimesheetRepository timesheetRepository;

    @Mock
    private TimesheetValidator timesheetValidator;

    @InjectMocks
    private TimesheetService timesheetService;

    @Test
    void addOrUpdateTimesheetCreateNewSuccess() {
        TimesheetRequest request = new TimesheetRequest();
        request.setEmployeeId("E1");
        request.setPayPeriodId(1);
        request.setHoursWorked(BigDecimal.TEN);
        request.setNoOfDaysWorked(1);

        doNothing().when(timesheetValidator).validateRequest(request);

        when(timesheetRepository.findByEmployeeIdAndPayPeriodId("E1", 1)).thenReturn(Optional.empty());

        TimesheetSummary savedEntity = TimesheetSummary.builder()
                .id(1L)
                .employeeId("E1")
                .payPeriodId(1)
                .hoursWorked(BigDecimal.TEN)
                .build();
        when(timesheetRepository.save(any(TimesheetSummary.class))).thenReturn(savedEntity);

        TimesheetResponse response = timesheetService.addOrUpdateTimesheet(request);

        assertNotNull(response);
        assertEquals("Timesheet created successfully", response.getMessage());
        assertEquals("E1", response.getEmployeeId());
    }

    @Test
    void addOrUpdateTimesheetUpdateExistingSuccess() {
        TimesheetRequest request = new TimesheetRequest();
        request.setEmployeeId("E1");
        request.setPayPeriodId(1);
        request.setHoursWorked(new BigDecimal("50")); // Updating hours

        doNothing().when(timesheetValidator).validateRequest(request);

        TimesheetSummary existing = TimesheetSummary.builder()
                .id(1L)
                .employeeId("E1")
                .payPeriodId(1)
                .hoursWorked(BigDecimal.TEN)
                .build();
        when(timesheetRepository.findByEmployeeIdAndPayPeriodId("E1", 1)).thenReturn(Optional.of(existing));
        when(timesheetRepository.save(any(TimesheetSummary.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TimesheetResponse response = timesheetService.addOrUpdateTimesheet(request);

        assertNotNull(response);
        assertEquals("Timesheet updated successfully", response.getMessage());
        assertEquals(new BigDecimal("50"), response.getHoursWorked()); // Verify update
    }
}
