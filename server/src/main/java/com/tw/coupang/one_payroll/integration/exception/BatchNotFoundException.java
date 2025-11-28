package com.tw.coupang.one_payroll.integration.exception;

public class BatchNotFoundException extends RuntimeException {
    public BatchNotFoundException(String batchId) {
        super("Batch not found with ID: " + batchId);
    }
}

