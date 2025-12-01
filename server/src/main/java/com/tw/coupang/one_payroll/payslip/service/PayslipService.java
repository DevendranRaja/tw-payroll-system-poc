package com.tw.coupang.one_payroll.payslip.service;

import com.tw.coupang.one_payroll.payslip.dto.PayslipMetadataDTO;
import com.tw.coupang.one_payroll.payslip.dto.PayslipResponse;

public interface PayslipService {
    PayslipMetadataDTO generatePayslipMetadata(String employeeId, String payPeriod);
    PayslipResponse getPayslipMetadata(String employeeId, String payPeriod);
}
