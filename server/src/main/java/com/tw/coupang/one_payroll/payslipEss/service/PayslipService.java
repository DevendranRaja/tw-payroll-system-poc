package com.tw.coupang.one_payroll.payslipEss.service;

import com.tw.coupang.one_payroll.payslipEss.dto.PayslipMetadataDTO;

import java.time.LocalDate;

public interface PayslipService {
    PayslipMetadataDTO generatePayslipMetadata(String employeeId, LocalDate payPeriod);
}
