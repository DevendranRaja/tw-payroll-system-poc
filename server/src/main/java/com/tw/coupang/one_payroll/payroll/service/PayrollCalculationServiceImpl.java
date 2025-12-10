package com.tw.coupang.one_payroll.payroll.service;

import com.tw.coupang.one_payroll.employee_master.entity.EmployeeMaster;
import com.tw.coupang.one_payroll.employee_master.enums.EmployeeStatus;
import com.tw.coupang.one_payroll.employee_master.exception.EmployeeInactiveException;
import com.tw.coupang.one_payroll.employee_master.service.EmployeeMasterService;
import com.tw.coupang.one_payroll.paygroups.entity.PayGroup;
import com.tw.coupang.one_payroll.paygroups.validator.PayGroupValidator;
import com.tw.coupang.one_payroll.payperiod.service.PayPeriodService;
import com.tw.coupang.one_payroll.payperiod.validator.PayPeriodCycleValidator;
import com.tw.coupang.one_payroll.payroll.dto.request.PayrollCalculationRequest;
import com.tw.coupang.one_payroll.payroll.dto.response.PayrollRunResponse;
import com.tw.coupang.one_payroll.payroll.entity.PayrollRun;
import com.tw.coupang.one_payroll.payroll.exception.PayrollRunAlreadyExistsException;
import com.tw.coupang.one_payroll.payroll.repository.PayrollRunRepository;
import com.tw.coupang.one_payroll.payroll.service.calculator.context.ProrationCalculatorContext;
import com.tw.coupang.one_payroll.payroll.service.calculator.proration.ProrationCalculatorFactory;
import com.tw.coupang.one_payroll.payroll.service.calculator.proration.ProrationPayCalculator;
import com.tw.coupang.one_payroll.payroll.validator.PayrollValidator;
import com.tw.coupang.one_payroll.timesheet.entity.TimesheetSummary;
import com.tw.coupang.one_payroll.timesheet.service.TimesheetService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static com.tw.coupang.one_payroll.common.utils.MathsUtils.percentOf;
import static com.tw.coupang.one_payroll.paygroups.constants.PayGroupConstants.DEFAULT_HOLIDAY_RATE;
import static com.tw.coupang.one_payroll.payroll.enums.PayrollStatus.PROCESSED;
import static com.tw.coupang.one_payroll.payroll.util.WorkingDaysUtil.getWeekdaysBetween;
import static java.math.RoundingMode.HALF_UP;

@Service
@AllArgsConstructor
@Slf4j
public class PayrollCalculationServiceImpl implements PayrollCalculationService {

    private final EmployeeMasterService employeeMasterService;
    private final PayGroupValidator payGroupValidator;
    private final PayrollRunRepository payrollRunRepository;
    private final PayrollValidator payrollValidator;
    private final PayPeriodCycleValidator payPeriodCycleValidator;
    private final PayPeriodService payPeriodService;
    private final TimesheetService timesheetService;
    private final ProrationCalculatorFactory prorationCalculatorFactory;

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

        payrollValidator.validateEmployeeJoiningDateAgainstPayPeriods(employee, startDate, endDate);
        payPeriodCycleValidator.validatePayPeriodsAgainstPayGroup(startDate, endDate, payGroup);
        log.info("Pay period validated for Employee ID: {}, Pay Group: {}, Period: {} to {}", employeeId, payGroup.getGroupName(), startDate, endDate);

        validateDuplicatePayrollRun(employeeId, startDate, endDate);

        final var payrollRun = PayrollRun.builder();
        payrollRun.employeeId(employee.getEmployeeId())
                .payPeriodStart(startDate)
                .payPeriodEnd(endDate);

        final Integer payPeriodId = payPeriodService.getPayPeriodId(payGroupId, startDate, endDate);
        TimesheetSummary timesheet = timesheetService.getTimesheet(employeeId, payPeriodId);

        BigDecimal proratedPay = calculateProrationGrossPay(employee, timesheet, getWeekdaysBetween(startDate, endDate), getHolidayRate(payGroup));

        //TODO: Use hours worked and pay group payment cycle to calculate gross pay
        payrollGrossToNetPayCalculation(proratedPay, payGroup, payrollRun);
        final var payrollRunFinal = payrollRun.build();
        payrollRunRepository.save(payrollRunFinal);
        //TODO: Send payrollRun data to Payslip, deductions, benefits tables.

        log.info("Payroll calculation completed for Employee ID: {}, Pay Period: {} to {}",
                request.getEmployeeId(), startDate, endDate);

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

    private void validateDuplicatePayrollRun(String employeeId, LocalDate startDate, LocalDate endDate) {
        if (payrollRunRepository.existsPayrollRunByEmployeeAndPeriod(employeeId, startDate, endDate)) {
            log.info("Duplicate payroll run attempted for employeeId={}, period={} to {}", employeeId, startDate, endDate);
            throw new PayrollRunAlreadyExistsException(employeeId, startDate, endDate);
        }
    }

    private BigDecimal getHolidayRate(PayGroup payGroup) {
        return payGroup.getHolidayRate() != null
                ? payGroup.getHolidayRate()
                : DEFAULT_HOLIDAY_RATE;
    }

    private BigDecimal calculateProrationGrossPay(EmployeeMaster employee, TimesheetSummary timesheet, int totalWorkingDaysInPayPeriod, BigDecimal holidayRate) {
        final ProrationCalculatorContext context = buildProrationCalculatorContext(totalWorkingDaysInPayPeriod, holidayRate);
        ProrationPayCalculator prorationPayCalculator = prorationCalculatorFactory.getCalculator(employee.getPayType());
        return prorationPayCalculator.calculate(employee, timesheet, context);
    }

    private ProrationCalculatorContext buildProrationCalculatorContext(int totalWorkingDaysInPayPeriod, BigDecimal holidayRate) {
        return ProrationCalculatorContext.builder()
                .totalWorkingDaysInPayPeriod(totalWorkingDaysInPayPeriod)
                .holidayRate(holidayRate)
                .build();
    }
}
