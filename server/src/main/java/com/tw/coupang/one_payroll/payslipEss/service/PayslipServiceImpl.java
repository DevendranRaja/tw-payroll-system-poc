package com.tw.coupang.one_payroll.payslipEss.service;

import com.tw.coupang.one_payroll.EmployeeMaster.Entity.EmployeeMaster;
import com.tw.coupang.one_payroll.EmployeeMaster.Enum.EmployeeStatus;
import com.tw.coupang.one_payroll.EmployeeMaster.Exception.EmployeeNotFoundException;
import com.tw.coupang.one_payroll.EmployeeMaster.Repository.EmployeeMasterRepository;
import com.tw.coupang.one_payroll.payslipEss.dto.PayslipItemDto;
import com.tw.coupang.one_payroll.payslipEss.dto.PayslipMetadataDTO;
import com.tw.coupang.one_payroll.payslipEss.entity.Payslip;
import com.tw.coupang.one_payroll.payslipEss.payrollmock.PayrollRun;
import com.tw.coupang.one_payroll.payslipEss.payrollmock.PayrollRunRepository;
import com.tw.coupang.one_payroll.payslipEss.repository.PayslipRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Slf4j
@Service
public class PayslipServiceImpl implements PayslipService
{
    private final PayrollRunRepository payrollRunRepository;
    private final EmployeeMasterRepository employeeMasterRepository;
    private final PayslipMetadataBuilder metadataBuilder;
    private final PayslipRepository payslipRepository;

    public PayslipServiceImpl(
            PayrollRunRepository payrollRunRepository,
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
        EmployeeMaster employee = employeeMasterRepository.findById(employeeId)
                .orElseThrow(() -> new EmployeeNotFoundException("Employee with ID '" + employeeId + "' not found"));

        if(employee.getStatus() != EmployeeStatus.ACTIVE) {
            throw new EmployeeNotFoundException("Employee with ID '" + employeeId + "' is not active");
        }

        //Get Pay Period
        LocalDate payPeriodEndOfMonth = YearMonth.parse(payPeriod).atEndOfMonth();

        PayrollRun payroll = payrollRunRepository
                .findPayrollForEmployeeIdAndPayPeriod(employeeId, payPeriod)
                .orElseThrow(() -> new IllegalStateException("Payroll not ready"));

        // Build metadata
        PayslipMetadataDTO payslipMetadata = metadataBuilder.buildPayslipMetadata(employee, payroll,payPeriodEndOfMonth);

        // Save or update payslip entity
        savePayslipMetadata(payslipMetadata);

        log.info("Payslip metadata successfully saved for employee: {}", employeeId);
        return payslipMetadata;
    }

    private void savePayslipMetadata(PayslipMetadataDTO payslipMetadata)
    {
        // Check if payslip already exists (idempotent)
        Payslip payslip = payslipRepository
                .findByEmployeeIdAndPayPeriod(payslipMetadata.getEmployeeId(),  payslipMetadata.getPayPeriod())
                .orElse(new Payslip());

        // Convert DTOs to entity classes
        List<PayslipItemDto> earnings = payslipMetadata.getEarnings().stream()
                .map(e -> PayslipItemDto.builder()
                        .description(e.getDescription())
                        .amount(e.getAmount())
                        .build())
                .toList();

        List<PayslipItemDto> deductions = payslipMetadata.getDeductions().stream()
                .map(e -> PayslipItemDto.builder()
                        .description(e.getDescription())
                        .amount(e.getAmount())
                        .build())
                .toList();

        // Update payslip fields
        payslip.setEmployeeId(payslipMetadata.getEmployeeId());
        payslip.setPayrollId(payslipMetadata.getPayrollId());
        payslip.setPayPeriod(payslipMetadata.getPayPeriod());
        payslip.setGrossPay(payslipMetadata.getGrossPay());
        payslip.setNetPay(payslipMetadata.getNetPay());
        payslip.setTax(payslipMetadata.getTaxAmount());
        payslip.setBenefits(payslipMetadata.getBenefitAmount());
        payslip.setEarnings(earnings);
        payslip.setDeductions(deductions);
        payslip.setFilePath(payslipMetadata.getFilePath());
        payslip.setCreatedAt(payslipMetadata.getCreatedAt());

        payslipRepository.save(payslip);
        log.info("Payslip entity saved for employee: {}", payslipMetadata.getEmployeeId());
    }


}
