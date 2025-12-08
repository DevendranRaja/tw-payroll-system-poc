package com.tw.coupang.one_payroll.payroll.service;

import com.tw.coupang.one_payroll.employee_master.entity.EmployeeMaster;
import com.tw.coupang.one_payroll.employee_master.enums.EmployeeStatus;
import com.tw.coupang.one_payroll.employee_master.enums.PayType;
import com.tw.coupang.one_payroll.employee_master.exception.EmployeeInactiveException;
import com.tw.coupang.one_payroll.employee_master.service.EmployeeMasterService;
import com.tw.coupang.one_payroll.paygroups.entity.PayGroup;
import com.tw.coupang.one_payroll.paygroups.validator.PayGroupValidator;
import com.tw.coupang.one_payroll.payperiod.entity.PayPeriod;
import com.tw.coupang.one_payroll.payperiod.exception.PayPeriodNotFoundException;
import com.tw.coupang.one_payroll.payperiod.repository.PayPeriodRepository;
import com.tw.coupang.one_payroll.payperiod.service.PayPeriodService;
import com.tw.coupang.one_payroll.payperiod.validator.PayPeriodCycleValidator;
import com.tw.coupang.one_payroll.payroll.dto.request.PayrollCalculationRequest;
import com.tw.coupang.one_payroll.payroll.dto.response.PayrollRunResponse;
import com.tw.coupang.one_payroll.payroll.entity.PayrollRun;
import com.tw.coupang.one_payroll.payroll.exception.InvalidPayrollStateException;
import com.tw.coupang.one_payroll.payroll.repository.PayrollRunRepository;
import com.tw.coupang.one_payroll.timesheet.entity.TimesheetSummary;
import com.tw.coupang.one_payroll.timesheet.exception.TimesheetNotFoundException;
import com.tw.coupang.one_payroll.timesheet.repository.TimesheetRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

import static com.tw.coupang.one_payroll.common.utils.MathsUtils.percentOf;
import static com.tw.coupang.one_payroll.common.utils.MathsUtils.safe;
import static com.tw.coupang.one_payroll.common.utils.MathsUtils.safeInt;
import static com.tw.coupang.one_payroll.payroll.enums.PayrollStatus.PROCESSED;
import static java.math.RoundingMode.HALF_UP;

@Service
@AllArgsConstructor
@Slf4j
public class PayrollCalculationServiceImpl implements PayrollCalculationService {

    private static final BigDecimal HOURS_PER_DAY = BigDecimal.valueOf(8);
    private static final BigDecimal DEFAULT_HOLIDAY_RATE = BigDecimal.valueOf(1.5);

    private final EmployeeMasterService employeeMasterService;
    private final PayGroupValidator payGroupValidator;
    private final PayrollRunRepository payrollRunRepository;
    private final PayPeriodCycleValidator payPeriodCycleValidator;
    private final TimesheetRepository timesheetRepository;
    private final PayPeriodRepository payPeriodRepository;
    private final PayPeriodService payPeriodService;

    @Override
    public PayrollRunResponse calculate(PayrollCalculationRequest request) {
        final String employeeId = request.getEmployeeId();
        log.info("Initiating payroll calculation for employeeId={}", employeeId);

        EmployeeMaster employee = employeeMasterService.getEmployeeById(employeeId);

        if(employee.getStatus() != EmployeeStatus.ACTIVE) {
            log.warn("Inactive employee attempted payroll calculation. employeeId={}", employeeId);
            throw new EmployeeInactiveException("Employee with ID '" + employeeId + "' is not active");
        }

        final Integer payGroupId = employee.getPayGroupId();
        final var payGroup = payGroupValidator.validatePayGroupExists(payGroupId);
        final LocalDate startDate = request.getPayPeriod().getStartDate();
        final LocalDate endDate = request.getPayPeriod().getEndDate();

        validateEmployeeJoiningDateAgainstPayPeriods(employee, startDate, endDate);
        payPeriodService.checkOverlap(payGroupId, startDate, endDate);

        log.info("Validated employee, pay group and pay periods for employeeId={}, payGroupId={}", employeeId, payGroupId);

        payPeriodCycleValidator.validatePayPeriodAgainstPayGroup(startDate, endDate, payGroup);

        log.info("Pay period validated for employeeId={} ({} → {})", employeeId, startDate, endDate);

        final var payrollRun = PayrollRun.builder();
        payrollRun.employeeId(employee.getEmployeeId())
                .payPeriodStart(request.getPayPeriod().getStartDate())
                .payPeriodEnd(request.getPayPeriod().getEndDate());

        final BigDecimal holidayRate = getHolidayRate(payGroup);
        final int totalWorkingDays = getWeekdaysBetween(startDate, endDate);
        PayPeriod payPeriod = getPayPeriodForEmployee(payGroupId, startDate, endDate);
        TimesheetSummary timesheet = fetchTimesheet(employeeId, payPeriod.getId());

        BigDecimal proratedPay = calculateProratedGrossPay(employee, totalWorkingDays, timesheet, holidayRate);

        //TODO: Use hours worked and pay group payment cycle to calculate gross pay
        payrollGrossToNetPayCalculation(proratedPay, payGroup, payrollRun);
        final var payrollRunFinal = payrollRun.build();
        payrollRunRepository.save(payrollRunFinal);
        //TODO: Send payrollRun data to Payslip, deductions, benefits tables.

        log.info("Payroll calculation completed for Employee ID: {}, Pay Period: {} to {}",
                request.getEmployeeId(), request.getPayPeriod().getStartDate(), request.getPayPeriod().getEndDate());

        return PayrollRunResponse.builder()
                .employeeId(payrollRunFinal.getEmployeeId())
                .payGroupId(payGroup.getId())
                .payPeriodStart(payrollRunFinal.getPayPeriodStart())
                .payPeriodEnd(payrollRunFinal.getPayPeriodEnd())
                .grossPay(payrollRunFinal.getGrossPay())
                .netPay(payrollRunFinal.getNetPay())
                .benefitsAmount(payrollRunFinal.getBenefitAddition())
                .taxAmount(payrollRunFinal.getTaxDeduction())
                .build();
    }

    public BigDecimal payrollGrossToNetPayCalculation(final BigDecimal grossPay,
                                                      final PayGroup payGroup,
                                                      final PayrollRun.PayrollRunBuilder payrollRun) {
        final var tax = percentOf(payGroup.getBaseTaxRate(), grossPay);
        final var benefits = percentOf(payGroup.getBenefitRate(), grossPay);
        final var otherDeductions = percentOf(payGroup.getDeductionRate(), grossPay);
        final var netPay = grossPay.subtract(tax).subtract(otherDeductions).add(benefits).setScale(2, HALF_UP);
        payrollRun.grossPay(grossPay)
                .netPay(netPay)
                .taxDeduction(tax)
                .benefitAddition(benefits)
                .status(PROCESSED);
        return netPay;
    }

    @Override
    public List<PayrollRunResponse> getPayroll(final String employeeId,
                                               final LocalDate periodStart,
                                               final LocalDate periodEnd) {
        final var payrollRuns = payrollRunRepository.findByEmployeeIdOrPayPeriodStartAndPayPeriodEnd(
                employeeId, periodStart, periodEnd);
        log.info("Fetched {} payroll records for Employee ID: {}, Period Start: {}, Period End: {}",
                payrollRuns.size(), employeeId, periodStart, periodEnd);
        return payrollRuns.stream().map(payrollRun -> PayrollRunResponse.builder()
                        .employeeId(payrollRun.getEmployeeId()).payPeriodStart(payrollRun.getPayPeriodStart())
                        .payPeriodEnd(payrollRun.getPayPeriodEnd())
                        .netPay(payrollRun.getNetPay()).grossPay(payrollRun.getGrossPay())
                        .benefitsAmount(payrollRun.getBenefitAddition()).taxAmount(payrollRun.getTaxDeduction())
                        .build())
                .toList();
    }

    private BigDecimal getHolidayRate(PayGroup payGroup) {
        return payGroup.getHolidayRate() != null
                ? payGroup.getHolidayRate()
                : DEFAULT_HOLIDAY_RATE;
    }

    private int getWeekdaysBetween(LocalDate start, LocalDate end) {
        int count = 0;
        for (LocalDate date = start; !date.isAfter(end); date = date.plusDays(1)) {
            DayOfWeek dow = date.getDayOfWeek();
            if (dow != DayOfWeek.SATURDAY && dow != DayOfWeek.SUNDAY) {
                count++;
            }
        }
        return count;
    }

    private PayPeriod getPayPeriodForEmployee(Integer payGroupId, LocalDate startDate, LocalDate endDate) {
        return payPeriodRepository
                .findByPayGroupIdAndPeriodStartDateAndPeriodEndDate(payGroupId, startDate, endDate)
                .orElseThrow(() -> new PayPeriodNotFoundException(payGroupId, startDate, endDate));
    }

    private TimesheetSummary fetchTimesheet(String employeeId, Integer payPeriodId) {
        return timesheetRepository
                .findByEmployeeIdAndPayPeriodId(employeeId, payPeriodId)
                .orElseThrow(() -> new TimesheetNotFoundException(employeeId, payPeriodId));
    }

    private BigDecimal calculateProratedGrossPay(EmployeeMaster employee, int totalWorkingDays, TimesheetSummary timesheet, BigDecimal holidayRate) {
        final BigDecimal basePay = fetchBasePayForEmployee(employee);
        if (employee.getPayType() == PayType.SALARIED) {
            return calculateSalariedProratedGross(basePay, totalWorkingDays, timesheet);
        } else {
            return calculateContractGross(basePay, timesheet, holidayRate);
        }
    }

    private BigDecimal fetchBasePayForEmployee(EmployeeMaster employee) {
        BigDecimal basePayPerDay = BigDecimal.valueOf(5000);

        if (employee.getPayType() == PayType.SALARIED)
            return basePayPerDay;
        else
            return basePayPerDay.divide(HOURS_PER_DAY, 2, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateSalariedProratedGross(BigDecimal basePayPerDay, int totalWorkingDays, TimesheetSummary timesheet) {
        int daysWorked = safeInt(timesheet.getNoOfDaysWorked());
        int holidayDays = safeInt(timesheet.getHolidayDays());

        int effectiveDays = daysWorked + holidayDays;

        BigDecimal prorated = basePayPerDay.multiply(BigDecimal.valueOf(effectiveDays))
                .divide(BigDecimal.valueOf(totalWorkingDays), 2, RoundingMode.HALF_UP);

        log.info("Salaried employee prorated pay: {} (workedDays={}, holidayDaysPaid={}, totalWorkingDays={})", prorated, daysWorked, holidayDays, totalWorkingDays);

        return prorated;
    }

    private BigDecimal calculateContractGross(BigDecimal basePayPerHour, TimesheetSummary timesheet, BigDecimal holidayRate) {
        BigDecimal hoursWorked = safe(timesheet.getHoursWorked());
        BigDecimal extraHoursWorked = safe(timesheet.getHolidayHoursWorked());

        BigDecimal gross = basePayPerHour.multiply(hoursWorked)
                .add(basePayPerHour.multiply(holidayRate).multiply(extraHoursWorked))
                .setScale(2, RoundingMode.HALF_UP);

        log.info("Hourly/Contract employee prorated pay: {} (hoursWorked={}, extraHoursWorked={})", gross, hoursWorked, extraHoursWorked);

        return gross;
    }

    private void validateEmployeeJoiningDateAgainstPayPeriods(EmployeeMaster employee, LocalDate periodStart, LocalDate periodEnd) {

        LocalDate joiningDate = employee.getJoiningDate();

        if (joiningDate.isAfter(periodEnd)) {
            throw new InvalidPayrollStateException(
                    String.format(
                            "Employee %s joined on %s which is after the pay period %s to %s",
                            employee.getEmployeeId(),
                            joiningDate,
                            periodStart,
                            periodEnd
                    )
            );
        }
    }
}
