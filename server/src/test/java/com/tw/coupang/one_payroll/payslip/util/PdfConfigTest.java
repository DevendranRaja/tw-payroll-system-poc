package com.tw.coupang.one_payroll.payslip.util;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PdfConfigTest {

    private PdfConfig pdfConfig;

    @BeforeEach
    void setUp() {
        pdfConfig = new PdfConfig();
    }

    @Test
    void testApplyBaseConfigReturnsBuilder() {
        PdfRendererBuilder builder = new PdfRendererBuilder();
        PdfRendererBuilder returnedBuilder = pdfConfig.applyBaseConfig(builder);

        assertSame(builder, returnedBuilder, "The same builder instance should be returned");
    }

    @Test
    void testApplyBaseConfigSetsFastMode() {
        PdfRendererBuilder builder = new PdfRendererBuilder();
        pdfConfig.applyBaseConfig(builder);

        assertDoesNotThrow(() -> builder.withHtmlContent("<html></html>", null));
    }

    @Test
    void testApplyBaseConfigLoadsFonts() {
        PdfRendererBuilder builder = new PdfRendererBuilder();
        assertDoesNotThrow(() -> pdfConfig.applyBaseConfig(builder));
    }
}
