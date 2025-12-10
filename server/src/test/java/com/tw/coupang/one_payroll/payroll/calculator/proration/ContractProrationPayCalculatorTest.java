package com.tw.coupang.one_payroll.payroll.calculator.proration;

import com.tw.coupang.one_payroll.employee_master.entity.EmployeeMaster;
import com.tw.coupang.one_payroll.payroll.service.calculator.context.ProrationCalculatorContext;
import com.tw.coupang.one_payroll.payroll.service.calculator.proration.ContractProrationPayCalculator;
import com.tw.coupang.one_payroll.timesheet.entity.TimesheetSummary;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static com.tw.coupang.one_payroll.paygroups.constants.PayGroupConstants.DEFAULT_HOLIDAY_RATE;
import static java.math.RoundingMode.HALF_UP;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class ContractProrationPayCalculatorTest {

    @InjectMocks
    private ContractProrationPayCalculator contractProrationPayCalculator;

    private EmployeeMaster employee;
    private TimesheetSummary timesheet;
    private ProrationCalculatorContext context;

    @BeforeEach
    void setUp() {
        employee = EmployeeMaster.builder()
                .employeeId("EMP001")
                .build();

        timesheet = TimesheetSummary.builder()
                .id(1L)
                .employeeId("EMP001")
                .build();

        context = ProrationCalculatorContext.builder()
                .holidayRate(DEFAULT_HOLIDAY_RATE)
                .build();
    }

    @Test
    void shouldCalculateRegularPayCorrectly() {
        timesheet.setHoursWorked(BigDecimal.valueOf(16));
        timesheet.setHolidayHoursWorked(BigDecimal.ZERO);

        BigDecimal expectedBasePayPerHour = BigDecimal.valueOf(5000)
                .divide(BigDecimal.valueOf(8), 2, HALF_UP);

        BigDecimal expectedPay = expectedBasePayPerHour.multiply(timesheet.getHoursWorked()).setScale(2, HALF_UP);

        BigDecimal actualPay = contractProrationPayCalculator.calculate(employee, timesheet, context);

        assertEquals(expectedPay, actualPay);
    }

    @Test
    void shouldIncludeHolidayPay() {
        timesheet.setHoursWorked(BigDecimal.valueOf(16));
        timesheet.setHolidayHoursWorked(BigDecimal.valueOf(8));

        BigDecimal basePayPerHour = BigDecimal.valueOf(5000).divide(BigDecimal.valueOf(8), 2, HALF_UP);
        BigDecimal expectedPay = basePayPerHour.multiply(timesheet.getHoursWorked())
                .add(basePayPerHour.multiply(context.holidayRate()).multiply(timesheet.getHolidayHoursWorked()))
                .setScale(2, HALF_UP);

        BigDecimal actualPay = contractProrationPayCalculator.calculate(employee, timesheet, context);

        assertEquals(expectedPay, actualPay);
    }

    @Test
    void shouldHandleNullHoursGracefully() {
        timesheet.setHoursWorked(null);
        timesheet.setHolidayHoursWorked(null);

        BigDecimal actualPay = contractProrationPayCalculator.calculate(employee, timesheet, context);

        assertEquals(BigDecimal.ZERO.setScale(2, HALF_UP), actualPay);
    }
}
