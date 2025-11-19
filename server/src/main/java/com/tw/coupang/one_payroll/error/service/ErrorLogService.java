package com.tw.coupang.one_payroll.error.service;

import com.tw.coupang.one_payroll.error.entity.ErrorLog;
import com.tw.coupang.one_payroll.error.enums.ErrorCode;
import com.tw.coupang.one_payroll.error.repository.ErrorLogRepository;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;

public class ErrorLogService {

    @Autowired
    private ErrorLogRepository errorLogRepository;

    public ErrorLog logError(String module, String employeeId, String errorCode)
    {
        String errorMessage =
                ErrorCode.valueOf(errorCode).getErrorMessage();

        ErrorLog errorLog = ErrorLog.builder()
                .moduleName(module)
                .errorMessage(errorMessage)
                .errorTime(LocalDateTime.now())
                .build();

        errorLog.setEmployeeId(employeeId);
        return errorLogRepository.save(errorLog);
    }
}
