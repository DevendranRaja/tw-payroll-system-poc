package com.tw.coupang.one_payroll.payslipEss.service;

import com.tw.coupang.one_payroll.payslipEss.dto.PayslipMetadataDTO;
import com.tw.coupang.one_payroll.payslipEss.dto.PayslipResponse;

public interface PayslipService {
    PayslipMetadataDTO generatePayslipMetadata(String employeeId, String payPeriod);
    PayslipResponse getPayslipMetadata(String employeeId, String payPeriod);
}
