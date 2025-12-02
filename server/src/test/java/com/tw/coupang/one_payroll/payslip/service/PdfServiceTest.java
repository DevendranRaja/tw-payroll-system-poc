package com.tw.coupang.one_payroll.payslip.service;

import com.tw.coupang.one_payroll.employee_master.exception.EmployeeNotFoundException;
import com.tw.coupang.one_payroll.payslip.dto.MonthlyPayslipSummaryDto;
import com.tw.coupang.one_payroll.payslip.dto.YtdSummaryForPdfDto;
import com.tw.coupang.one_payroll.payslip.dto.YtdSummaryResponse;
import com.tw.coupang.one_payroll.payslip.dto.PayslipMetadataDTO;
import com.tw.coupang.one_payroll.payslip.util.PdfGenerator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PdfServiceTest {

    @Mock
    private PdfGenerator pdfGenerator;

    @Mock
    private PayslipService payslipService;

    @Mock
    private YtdSummaryService ytdSummaryService;

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

    @Test
    @SuppressWarnings("unchecked")
    void generateYtdPdfSuccess() {
        String employeeId = "E001";
        int year = 2025;

        Map<String, MonthlyPayslipSummaryDto> monthlyBreakdown = new HashMap<>();
        monthlyBreakdown.put("JANUARY", new MonthlyPayslipSummaryDto(
                "JANUARY", 1, year,
                BigDecimal.valueOf(1000),
                BigDecimal.valueOf(800),
                BigDecimal.valueOf(150),
                BigDecimal.valueOf(50)
        ));

        YtdSummaryResponse ytdTotals = new YtdSummaryResponse(
                BigDecimal.valueOf(16500),
                BigDecimal.valueOf(14900),
                BigDecimal.valueOf(1300),
                BigDecimal.valueOf(300)
        );

        YtdSummaryForPdfDto ytdSummary = new YtdSummaryForPdfDto(
                employeeId,
                "John Doe",
                "Engineering",
                "Developer",
                year,
                monthlyBreakdown,
                ytdTotals
        );

        byte[] pdfData = new byte[]{5, 6, 7, 8};

        when(ytdSummaryService.getYtdSummaryWithBreakdown(employeeId, year)).thenReturn(ytdSummary);
        when(pdfGenerator.generateYtdPdf(anyMap())).thenReturn(pdfData);

        byte[] result = pdfService.generateYtdPdf(employeeId, year);

        assertArrayEquals(pdfData, result);

        ArgumentCaptor<Map<String, Object>> captor = ArgumentCaptor.forClass(Map.class);
        verify(pdfGenerator).generateYtdPdf(captor.capture());

        Map<String, Object> capturedModel = captor.getValue();
        assertEquals(ytdSummary, capturedModel.get("ytd"));
        assertNotNull(capturedModel.get("monthlyList"));

        verify(ytdSummaryService, times(1)).getYtdSummaryWithBreakdown(employeeId, year);
        verify(pdfGenerator, times(1)).generateYtdPdf(anyMap());
    }

    @Test
    void generateYtdPdfHandlesMissingMonths() {
        String employeeId = "E002";
        int year = 2025;
        YtdSummaryForPdfDto ytdSummary = new YtdSummaryForPdfDto(
                employeeId,
                "Jane Doe",
                "Finance",
                "Analyst",
                year,
                null, // monthlyBreakdown is null
                new YtdSummaryResponse(
                        BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO
                )
        );

        when(ytdSummaryService.getYtdSummaryWithBreakdown(employeeId, year)).thenReturn(ytdSummary);
        when(pdfGenerator.generateYtdPdf(anyMap())).thenReturn(new byte[]{9, 10, 11});

        byte[] result = pdfService.generateYtdPdf(employeeId, year);
        assertNotNull(result);
        verify(pdfGenerator, times(1)).generateYtdPdf(anyMap());
    }
}
