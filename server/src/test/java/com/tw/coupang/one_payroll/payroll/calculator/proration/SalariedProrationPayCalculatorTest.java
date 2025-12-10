package com.tw.coupang.one_payroll.payroll.calculator.proration;

import com.tw.coupang.one_payroll.employee_master.entity.EmployeeMaster;
import com.tw.coupang.one_payroll.payroll.service.calculator.context.ProrationCalculatorContext;
import com.tw.coupang.one_payroll.payroll.service.calculator.proration.SalariedProrationPayCalculator;
import com.tw.coupang.one_payroll.timesheet.entity.TimesheetSummary;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static java.math.RoundingMode.HALF_UP;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class SalariedProrationPayCalculatorTest {

    @InjectMocks
    private SalariedProrationPayCalculator salariedProrationPayCalculator;

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
                .totalWorkingDaysInPayPeriod(22)
                .build();
    }

    @ParameterizedTest
    @CsvSource({
            "10, 0, 10",
            "10, 2, 12",
            "0,  0, 0"
    })
    void shouldCalculateProratedPay(int workedDays, int holidayDays, int expectedEffectiveDays) {
        timesheet.setNoOfDaysWorked(workedDays == 0 ? null : workedDays);
        timesheet.setHolidayDays(holidayDays == 0 ? null : holidayDays);

        BigDecimal basePayPerMonth = BigDecimal.valueOf(50000);

        BigDecimal expectedPay = basePayPerMonth
                .multiply(BigDecimal.valueOf(expectedEffectiveDays))
                .divide(BigDecimal.valueOf(context.totalWorkingDaysInPayPeriod()), 2, HALF_UP);

        BigDecimal actualPay = salariedProrationPayCalculator.calculate(employee, timesheet, context);

        assertEquals(expectedPay, actualPay);
    }

    @Test
    void shouldRoundToTwoDecimalPlaces() {
        timesheet.setNoOfDaysWorked(7);
        timesheet.setHolidayDays(3);

        BigDecimal basePayPerMonth = BigDecimal.valueOf(50000);
        BigDecimal expectedPay = basePayPerMonth
                .multiply(BigDecimal.valueOf(10))
                .divide(BigDecimal.valueOf(context.totalWorkingDaysInPayPeriod()), 2, HALF_UP);

        BigDecimal actualPay = salariedProrationPayCalculator.calculate(employee, timesheet, context);

        assertEquals(expectedPay, actualPay);
    }
}
