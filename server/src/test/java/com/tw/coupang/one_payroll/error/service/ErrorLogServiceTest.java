package com.tw.coupang.one_payroll.error.service;

import com.tw.coupang.one_payroll.error.entity.ErrorLog;
import com.tw.coupang.one_payroll.error.enums.ErrorCode;
import com.tw.coupang.one_payroll.error.repository.ErrorLogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class ErrorLogServiceTest {

    @Mock
    private ErrorLogRepository errorLogRepository;

    @InjectMocks
    private ErrorLogService errorLoggingService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void callRepositorySaveToSaveErrorLogs()
    {
        String module = "Employee_Master";
        String employeeId = "E001";
        String errorCode = "MISSING_MANDATORY_FIELD";
        String errorMessage = ErrorCode.MISSING_MANDATORY_FIELD.getErrorMessage();

        when(errorLogRepository.save(any(ErrorLog.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ErrorLog savedLog = errorLoggingService.logError(module, employeeId, errorCode);

        assertNotNull(savedLog);
        assertEquals(module, savedLog.getModuleName());
        assertEquals(employeeId, savedLog.getEmployeeId());
        assertEquals(errorMessage, savedLog.getErrorMessage());
        assertNotNull(savedLog.getErrorTime());

        verify(errorLogRepository, times(1)).save(any(ErrorLog.class));
    }
}
