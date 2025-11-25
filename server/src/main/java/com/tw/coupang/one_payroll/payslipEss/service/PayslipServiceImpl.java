package com.tw.coupang.one_payroll.payslipEss.service;

import com.tw.coupang.one_payroll.EmployeeMaster.Entity.EmployeeMaster;
import com.tw.coupang.one_payroll.EmployeeMaster.Enum.EmployeeStatus;
import com.tw.coupang.one_payroll.EmployeeMaster.Exception.EmployeeNotFoundException;
import com.tw.coupang.one_payroll.EmployeeMaster.Repository.EmployeeMasterRepository;
import com.tw.coupang.one_payroll.payslipEss.dto.PayslipMetadataDTO;
import com.tw.coupang.one_payroll.payslipEss.payrollmock.PayrollRun;
import com.tw.coupang.one_payroll.payslipEss.payrollmock.PayrollRunRepository;
import com.tw.coupang.one_payroll.payslipEss.repository.PayslipRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

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
    public PayslipMetadataDTO generatePayslipMetadata(String employeeId, LocalDate payPeriod)
    {
        EmployeeMaster employee = employeeMasterRepository.findById(employeeId)
                .orElseThrow(() -> new EmployeeNotFoundException("Employee with ID '" + employeeId + "' not found"));

        if(employee.getStatus() != EmployeeStatus.ACTIVE) {
            throw new EmployeeNotFoundException("Employee with ID '" + employeeId + "' is not active");
        }

        PayrollRun payroll = payrollRunRepository
                .findByEmployeeIdAndPayPeriodEnd(employeeId, payPeriod)
                .orElseThrow(() -> new IllegalStateException("Payroll not ready"));

        // Build metadata
        PayslipMetadataDTO payslipMetadata = metadataBuilder.buildPayslipMetadata(employee, payroll);

        // Save or update payslip entity
        savePayslipEntity(payslipMetadata);

        log.info("Payslip metadata successfully saved for employee: {}", employeeId);
        return payslipMetadata;
    }

    private void savePayslipEntity(PayslipMetadataDTO metadata) {
    }


}
