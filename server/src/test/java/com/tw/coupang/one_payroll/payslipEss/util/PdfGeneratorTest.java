package com.tw.coupang.one_payroll.payslipEss.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PdfGeneratorTest {

    @Mock
    private TemplateEngine templateEngine;

    @InjectMocks
    private PdfGenerator pdfGenerator;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGeneratePayslipPdfReturnsNonEmptyByteArray() throws Exception {
        Map<String, Object> model = new HashMap<>();
        model.put("employee", "E001");
        model.put("period", "2025-11");

        String mockHtml = "<html><body>Payslip</body></html>";
        when(templateEngine.process(eq("payslip"), any(Context.class))).thenReturn(mockHtml);

        byte[] result = pdfGenerator.generatePayslipPdf(model);

        assertNotNull(result);
        assertTrue(result.length > 0);

        verify(templateEngine, times(1)).process(eq("payslip"), any(Context.class));
    }

    @Test
    void testGeneratePayslipPdfHandlesExceptionGracefully() throws Exception {
        Map<String, Object> model = new HashMap<>();
        model.put("employee", "E002");

        when(templateEngine.process(eq("payslip"), any(Context.class))).thenThrow(new RuntimeException("Template error"));

        byte[] result = pdfGenerator.generatePayslipPdf(model);

        assertNotNull(result);
        assertEquals(0, result.length);

        verify(templateEngine, times(1)).process(eq("payslip"), any(Context.class));
    }
}
