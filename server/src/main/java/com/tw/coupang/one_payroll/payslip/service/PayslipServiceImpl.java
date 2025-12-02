package com.tw.coupang.one_payroll.payslip.service;

import com.tw.coupang.one_payroll.employee_master.entity.EmployeeMaster;
import com.tw.coupang.one_payroll.employee_master.enums.EmployeeStatus;
import com.tw.coupang.one_payroll.employee_master.exception.EmployeeNotFoundException;
import com.tw.coupang.one_payroll.employee_master.repository.EmployeeMasterRepository;
import com.tw.coupang.one_payroll.integration.entity.PayrollRun;
import com.tw.coupang.one_payroll.payslip.dto.PayslipMetadataDTO;
import com.tw.coupang.one_payroll.payslip.dto.PayslipResponse;
import com.tw.coupang.one_payroll.payslip.entity.Payslip;
import com.tw.coupang.one_payroll.payslip.exception.PayslipNotFoundException;
import com.tw.coupang.one_payroll.payslip.payrollmock.PayrollRunMockRepository;
import com.tw.coupang.one_payroll.payslip.repository.PayslipRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;

@Slf4j
@Service
public class PayslipServiceImpl implements PayslipService
{
    private final PayrollRunMockRepository payrollRunRepository;
    private final EmployeeMasterRepository employeeMasterRepository;
    private final PayslipMetadataBuilder metadataBuilder;
    private final PayslipRepository payslipRepository;

    public PayslipServiceImpl(
            PayrollRunMockRepository payrollRunRepository,
            EmployeeMasterRepository employeeMasterRepository,
            PayslipMetadataBuilder metadataBuilder,
            PayslipRepository payslipRepository)
    {
        this.payrollRunRepository = payrollRunRepository;
        this.employeeMasterRepository = employeeMasterRepository;
        this.metadataBuilder = metadataBuilder;
        this.payslipRepository = payslipRepository;
    }

    @Override
    @Transactional
    public PayslipMetadataDTO generatePayslipMetadata(String employeeId, String payPeriod)
    {
        log.info("Generating payslip for employee: {}, period: {}", employeeId, payPeriod);

        EmployeeMaster employee = employeeMasterRepository.findById(employeeId)
                .orElseThrow(() -> new EmployeeNotFoundException("Employee with ID '" + employeeId + "' not found"));

        if(employee.getStatus() != EmployeeStatus.ACTIVE) {
            throw new EmployeeNotFoundException("Employee with ID '" + employeeId + "' is not active");
        }

        //Get Pay Period
        LocalDate payPeriodEndOfMonth;
        try {
            payPeriodEndOfMonth = YearMonth.parse(payPeriod).atEndOfMonth();
        } catch (Exception ex) {
            throw new IllegalArgumentException(
                    "Invalid pay period: '" + payPeriod + "'. Expected format is YYYY-MM."
            );
        }
        PayrollRun payroll = payrollRunRepository
                .findPayrollForEmployeeIdAndPayPeriod(employeeId, payPeriod)
                .orElseThrow(() -> new IllegalStateException("Payroll not ready"));

        // Build metadata
        PayslipMetadataDTO payslipMetadata = metadataBuilder.buildPayslipMetadata(employee, payroll,payPeriodEndOfMonth);

        // Save payslip metadata
        savePayslipMetadata(payslipMetadata);

        log.info("Payslip metadata successfully saved for employee: {}", employeeId);
        return payslipMetadata;
    }

    private void savePayslipMetadata(PayslipMetadataDTO payslipMetadata)
    {
        log.info("Going to save Payslip metadata");
        // Check if payslip already exists (idempotent)
        Payslip payslip = payslipRepository
                .findByEmployeeIdAndPayPeriod(payslipMetadata.getEmployeeId(),  payslipMetadata.getPayPeriod())
                .orElse(new Payslip());

        // Update payslip fields
        payslip.setEmployeeId(payslipMetadata.getEmployeeId());
        payslip.setPayrollId(payslipMetadata.getPayrollId());
        payslip.setPayPeriod(payslipMetadata.getPayPeriod());
        payslip.setGrossPay(payslipMetadata.getGrossPay());
        payslip.setNetPay(payslipMetadata.getNetPay());
        payslip.setBenefits(payslipMetadata.getBenefitAmount());
        payslip.setEarnings(payslipMetadata.getEarnings());
        payslip.setDeductions(payslipMetadata.getDeductions());
        payslip.setFilePath(payslipMetadata.getFilePath());
        payslip.setCreatedAt(payslipMetadata.getCreatedAt());

        payslipRepository.save(payslip);
        log.info("Payslip entity saved for employee: {}", payslipMetadata.getEmployeeId());
    }

    @Override
    @Transactional(readOnly = true)
    public PayslipResponse getPayslipMetadata(String employeeId, String payPeriod)
    {
        log.info("Fetching payslip for employee: {}, period: {}", employeeId, payPeriod);

        Payslip payslip = payslipRepository.findByEmployeeIdAndYearMonth(employeeId, payPeriod)
                .orElseThrow(() -> new PayslipNotFoundException("Payslip for employeeID '" + employeeId +
                        "' for the period '" + payPeriod + "' not found!"));

        log.info("Payslip fetched: {}", payslip);

       return convertToDTO(payslip);
    }

    private PayslipResponse convertToDTO(Payslip payslip)
    {
        return PayslipResponse.builder()
                .employeeId(payslip.getEmployeeId())
                .period(payslip.getPayPeriod().toString())
                .grossPay(payslip.getGrossPay())
                .netPay(payslip.getNetPay())
                .earnings(payslip.getEarnings())
                .deductions(payslip.getDeductions())
                .createdAt(payslip.getCreatedAt())
                .build();
    }
}
