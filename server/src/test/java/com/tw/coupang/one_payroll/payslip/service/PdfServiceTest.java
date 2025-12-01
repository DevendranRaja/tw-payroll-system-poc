package com.tw.coupang.one_payroll.payslip.service;

import com.tw.coupang.one_payroll.EmployeeMaster.Exception.EmployeeNotFoundException;
import com.tw.coupang.one_payroll.payslip.dto.PayslipMetadataDTO;
import com.tw.coupang.one_payroll.payslip.util.PdfGenerator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PdfServiceTest {

    @Mock
    private PdfGenerator pdfGenerator;

    @Mock
    private PayslipService payslipService;

    @InjectMocks
    private PdfService pdfService;

    private AutoCloseable mocks;

    @BeforeEach
    void setUp() {
        mocks = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void tearDown() throws Exception {
        mocks.close();
    }

    @Test
    @SuppressWarnings("unchecked")
    void generatePayslipPdfSuccess() {
        String employeeId = "E001";
        String period = "2025-11";
        PayslipMetadataDTO metadata = new PayslipMetadataDTO();
        byte[] pdfData = new byte[]{1, 2, 3};

        when(payslipService.generatePayslipMetadata(employeeId, period)).thenReturn(metadata);
        when(pdfGenerator.generatePayslipPdf(anyMap())).thenReturn(pdfData);

        byte[] result = pdfService.generatePayslipPdf(employeeId, period);

        assertArrayEquals(pdfData, result);

        ArgumentCaptor<Map<String, Object>> captor = ArgumentCaptor.forClass(Map.class);
        verify(pdfGenerator).generatePayslipPdf(captor.capture());

        Map<String, Object> capturedModel = captor.getValue();
        assertEquals(metadata, capturedModel.get("payslip"));

        verify(payslipService, times(1)).generatePayslipMetadata(employeeId, period);
        verify(pdfGenerator, times(1)).generatePayslipPdf(anyMap());
    }

    @Test
    void generatePayslipPdfServiceThrowsException() {
        String employeeId = "E002";
        String period = "2025-10";

        when(payslipService.generatePayslipMetadata(employeeId, period))
                .thenThrow(new RuntimeException("Metadata generation failed"));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> pdfService.generatePayslipPdf(employeeId, period));

        assertEquals("Metadata generation failed", ex.getMessage());
        verify(payslipService, times(1)).generatePayslipMetadata(employeeId, period);
        verify(pdfGenerator, never()).generatePayslipPdf(anyMap());
    }

    @Test
    void generatePayslipPdfShouldThrowExceptionWhenEmployeeIdIsNull() {
        String employeeId = null;
        String period = "2025-11";

        when(payslipService.generatePayslipMetadata(employeeId, period))
                .thenThrow(new EmployeeNotFoundException("Employee with ID 'null' not found"));

        EmployeeNotFoundException ex = assertThrows(EmployeeNotFoundException.class,
                () -> pdfService.generatePayslipPdf(employeeId, period));

        assertTrue(ex.getMessage().contains("Employee with ID 'null' not found"));
        verify(payslipService, times(1)).generatePayslipMetadata(employeeId, period);
        verify(pdfGenerator, never()).generatePayslipPdf(anyMap());
    }

    @Test
    void generatePayslipPdfShouldThrowExceptionWhenPeriodIsNull() {
        String employeeId = "E003";
        String period = null;

        when(payslipService.generatePayslipMetadata(employeeId, period))
                .thenThrow(new IllegalArgumentException("Invalid pay period"));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> pdfService.generatePayslipPdf(employeeId, period));

        assertTrue(ex.getMessage().contains("Invalid pay period"));
        verify(payslipService, times(1)).generatePayslipMetadata(employeeId, period);
        verify(pdfGenerator, never()).generatePayslipPdf(anyMap());
    }
}
