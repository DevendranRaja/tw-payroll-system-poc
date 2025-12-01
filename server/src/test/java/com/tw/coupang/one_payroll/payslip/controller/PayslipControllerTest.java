package com.tw.coupang.one_payroll.payslip.controller;

import com.tw.coupang.one_payroll.payslip.dto.PayslipResponse;
import com.tw.coupang.one_payroll.payslip.service.PayslipService;
import com.tw.coupang.one_payroll.payslip.service.PdfService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PaySlipControllerTest {

    @Mock
    private PayslipService payslipService;

    @Mock
    private PdfService pdfService;

    @InjectMocks
    private PaySlipController controller;

    private AutoCloseable mocks;

    @BeforeEach
    void setUp() {
        mocks = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void tearDown() throws Exception {
        if (mocks != null) mocks.close();
    }

    @Test
    void getPayslipSuccess() {
        String empId = "E001";
        String period = "2025-11";

        PayslipResponse response = PayslipResponse.builder()
                .employeeId(empId)
                .period(period)
                .earnings(Collections.singletonMap("Basic", BigDecimal.valueOf(5000)))
                .deductions(Collections.singletonMap("Tax", BigDecimal.valueOf(500)))
                .grossPay(BigDecimal.valueOf(5000))
                .netPay(BigDecimal.valueOf(4500))
                .createdAt(LocalDateTime.now())
                .build();

        when(payslipService.getPayslipMetadata(empId, period)).thenReturn(response);

        PayslipResponse result = controller.getPayslip(empId, period);

        assertNotNull(result);
        assertEquals(empId, result.getEmployeeId());
        assertEquals(period, result.getPeriod());
        assertEquals(BigDecimal.valueOf(5000), result.getGrossPay());

        verify(payslipService, times(1)).getPayslipMetadata(empId, period);
    }

    @Test
    void getPayslipServiceThrowsException() {
        String empId = "E002";
        String period = "2025-10";

        when(payslipService.getPayslipMetadata(empId, period))
                .thenThrow(new RuntimeException("Payslip not found"));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> controller.getPayslip(empId, period));

        assertEquals("Payslip not found", ex.getMessage());
        verify(payslipService, times(1)).getPayslipMetadata(empId, period);
    }

    @Test
    void downloadPayslipSuccess() {
        String empId = "E001";
        String period = "2025-11";
        byte[] pdfData = new byte[]{1, 2, 3, 4};

        when(pdfService.generatePayslipPdf(empId, period)).thenReturn(pdfData);

        ResponseEntity<byte[]> response = controller.downloadPayslip(empId, period);

        assertEquals(200, response.getStatusCode().value());
        assertArrayEquals(pdfData, response.getBody());
        assertEquals(MediaType.APPLICATION_PDF, response.getHeaders().getContentType());

        List<String> dispositionHeaders = response.getHeaders().get(HttpHeaders.CONTENT_DISPOSITION);
        assertNotNull(dispositionHeaders);
        assertTrue(dispositionHeaders.stream().anyMatch(h -> h.contains("payslip_" + period + ".pdf")));

        verify(pdfService, times(1)).generatePayslipPdf(empId, period);
    }

    @Test
    void downloadPayslipServiceThrowsException() {
        String empId = "E003";
        String period = "2025-09";

        when(pdfService.generatePayslipPdf(empId, period))
                .thenThrow(new RuntimeException("PDF generation failed"));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> controller.downloadPayslip(empId, period));

        assertEquals("PDF generation failed", ex.getMessage());
        verify(pdfService, times(1)).generatePayslipPdf(empId, period);
    }
}
