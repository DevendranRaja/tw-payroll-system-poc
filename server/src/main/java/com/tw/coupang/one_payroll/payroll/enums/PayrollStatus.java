package com.tw.coupang.one_payroll.payroll.enums;

public enum PayrollStatus {
    PROCESSED,      // Ready to be sent
    SUBMITTED,      // Successfully accepted by Bank/SAP
    SUBMISSION_FAILED, // Bank rejected the batch
    FAILED // Processing failed
}
