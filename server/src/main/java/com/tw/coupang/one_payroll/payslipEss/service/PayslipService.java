package com.tw.coupang.one_payroll.payslipEss.service;

import com.tw.coupang.one_payroll.payslipEss.dto.PayslipMetadataDTO;

public interface PayslipService {
    PayslipMetadataDTO generatePayslipMetadata(String employeeId, String payPeriod);
}
