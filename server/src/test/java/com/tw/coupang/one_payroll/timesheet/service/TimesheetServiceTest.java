package com.tw.coupang.one_payroll.timesheet.service;

import com.tw.coupang.one_payroll.timesheet.dto.TimesheetRequest;
import com.tw.coupang.one_payroll.timesheet.dto.TimesheetResponse;
import com.tw.coupang.one_payroll.timesheet.entity.TimesheetSummary;
import com.tw.coupang.one_payroll.timesheet.exception.TimesheetNotFoundException;
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
        request.setHolidayDays(0);
        request.setHolidayHoursWorked(BigDecimal.ZERO);

        doNothing().when(timesheetValidator).validateRequest(request);

        when(timesheetRepository.findByEmployeeIdAndPayPeriodId("E1", 1)).thenReturn(Optional.empty());

        TimesheetSummary savedEntity = TimesheetSummary.builder()
                .id(1L)
                .employeeId("E1")
                .payPeriodId(1)
                .hoursWorked(BigDecimal.TEN)
                .holidayDays(0)
                .holidayHoursWorked(BigDecimal.ZERO)
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
        request.setHolidayDays(2);
        request.setHolidayHoursWorked(BigDecimal.ZERO);

        doNothing().when(timesheetValidator).validateRequest(request);

        TimesheetSummary existing = TimesheetSummary.builder()
                .id(1L)
                .employeeId("E1")
                .payPeriodId(1)
                .holidayDays(2)
                .holidayHoursWorked(BigDecimal.ZERO)
                .hoursWorked(BigDecimal.TEN)
                .build();
        when(timesheetRepository.findByEmployeeIdAndPayPeriodId("E1", 1)).thenReturn(Optional.of(existing));
        when(timesheetRepository.save(any(TimesheetSummary.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TimesheetResponse response = timesheetService.addOrUpdateTimesheet(request);

        assertNotNull(response);
        assertEquals("Timesheet updated successfully", response.getMessage());
        assertEquals(new BigDecimal("50"), response.getHoursWorked()); // Verify update
    }

    @Test
    void getTimesheetWhenExistsShouldReturnTimesheet() {
        String employeeId = "EMP123";
        Integer payPeriodId = 1;

        TimesheetSummary summary = TimesheetSummary.builder()
                .id(100L)
                .employeeId(employeeId)
                .payPeriodId(payPeriodId)
                .hoursWorked(BigDecimal.TEN)
                .holidayDays(2)
                .holidayHoursWorked(BigDecimal.ZERO)
                .build();

        when(timesheetRepository.findByEmployeeIdAndPayPeriodId(employeeId, payPeriodId))
                .thenReturn(Optional.of(summary));

        TimesheetSummary result = timesheetService.getTimesheet(employeeId, payPeriodId);

        assertNotNull(result);
        assertEquals(employeeId, result.getEmployeeId());
        assertEquals(payPeriodId, result.getPayPeriodId());
        assertEquals(BigDecimal.TEN, result.getHoursWorked());
    }

    @Test
    void getTimesheetWhenNotExistsShouldThrowException() {
        String employeeId = "EMP123";
        Integer payPeriodId = 1;

        when(timesheetRepository.findByEmployeeIdAndPayPeriodId(employeeId, payPeriodId))
                .thenReturn(Optional.empty());

        TimesheetNotFoundException exception = assertThrows(
                TimesheetNotFoundException.class,
                () -> timesheetService.getTimesheet(employeeId, payPeriodId)
        );

        assertTrue(exception.getMessage().contains(employeeId));
        assertTrue(exception.getMessage().contains(payPeriodId.toString()));
    }
}
