package com.tw.coupang.one_payroll.payslip.util;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.ByteArrayOutputStream;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class PdfGenerator {

    private final TemplateEngine templateEngine;
    private final PdfConfig pdfConfig;

    public byte[] generatePayslipPdf(Map<String, Object> model) {
        return generatePdfFromTemplate(model, "payslip");
    }

    public byte[] generateYtdPdf(Map<String, Object> model) {
        return generatePdfFromTemplate(model, "ytd");
    }

    private byte[] generatePdfFromTemplate(Map<String, Object> model, String templateName) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            Context context = new Context();
            context.setVariables(model);

            String html = templateEngine.process(templateName, context);

            PdfRendererBuilder builder = new PdfRendererBuilder();

            pdfConfig.applyBaseConfig(builder);

            builder.withHtmlContent(html, null);
            builder.toStream(outputStream);
            builder.run();

            return outputStream.toByteArray();

        } catch (Exception e) {
            log.error("PDF generation failed for '{}': {}", templateName, e.getMessage(), e);
            return new byte[0];
        }
    }
}
