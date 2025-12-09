package com.tw.coupang.one_payroll.integration.service;

import com.tw.coupang.one_payroll.integration.dto.PayrollBatchLogResponse;
import com.tw.coupang.one_payroll.integration.entity.PayrollBatchLog;
import com.tw.coupang.one_payroll.integration.exception.MandatoryFieldMissingException;
import com.tw.coupang.one_payroll.integration.repository.PayrollBatchLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
@Slf4j
public class PayrollBatchLogService {

    private final PayrollBatchLogRepository payrollBatchLogRepository;

    public Page<PayrollBatchLogResponse> getBatchLogs(String batchId, String employeeId, Pageable pageable) {
        if (!StringUtils.hasText(batchId) && !StringUtils.hasText(employeeId)) {
            throw new MandatoryFieldMissingException("batchId or employeeId");
        }

        Page<PayrollBatchLog> logsPage;
        if (StringUtils.hasText(batchId) && StringUtils.hasText(employeeId)) {
            log.info("Fetching logs for Batch ID: {} and Employee ID: {} with pagination: {}", batchId, employeeId, pageable);
            logsPage = payrollBatchLogRepository.findByBatchRefIdAndEmployeeId(batchId, employeeId, pageable);
        } else if (StringUtils.hasText(batchId)) {
            log.info("Fetching all logs for Batch ID: {} with pagination: {}", batchId, pageable);
            logsPage = payrollBatchLogRepository.findByBatchRefId(batchId, pageable);
        } else {
            log.info("Fetching all logs for Employee ID: {} with pagination: {}", employeeId, pageable);
            logsPage = payrollBatchLogRepository.findByEmployeeId(employeeId, pageable);
        }

        return logsPage.map(this::convertToDto);
    }

    private PayrollBatchLogResponse convertToDto(PayrollBatchLog logEntity) {
        return PayrollBatchLogResponse.builder()
                .batchRefId(logEntity.getBatchRefId())
                .employeeId(logEntity.getEmployeeId())
                .status(logEntity.getStatus())
                .logMessage(logEntity.getLogMessage())
                .timestamp(logEntity.getTimestamp())
                .build();
    }
}