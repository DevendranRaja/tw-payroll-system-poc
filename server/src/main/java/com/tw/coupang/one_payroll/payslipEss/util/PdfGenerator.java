package com.tw.coupang.one_payroll.payslipEss.util;

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

    public byte[] generatePayslipPdf(Map<String, Object> model) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            Context context = new Context();
            context.setVariables(model);

            String html = templateEngine.process("payslip", context);

            log.info("Final Thymeleaf HTML:\n{}", html);

            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();
            builder.withHtmlContent(html, null);
            builder.toStream(outputStream);
            builder.run();

            return outputStream.toByteArray();

        } catch (Exception e) {
            log.error("PDF generation failed", e);
            return new byte[0];
        }
    }
}
