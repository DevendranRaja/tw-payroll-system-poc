package com.tw.coupang.one_payroll.payslipEss.service;

import com.tw.coupang.one_payroll.payslipEss.dto.PayslipMetadataDTO;
import com.tw.coupang.one_payroll.payslipEss.util.PdfGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class PdfService {

    private final PdfGenerator pdfGenerator;
    private final PayslipService payslipService;

    public byte[] generatePayslipPdf(String employeeId, String period) {
        log.info("Generating payslip PDF for employeeId={} and period={}", employeeId, period);

        PayslipMetadataDTO response = payslipService.generatePayslipMetadata(employeeId, period);

        Map<String, Object> model = new HashMap<>();
        model.put("payslip", response);

        byte[] pdfBytes = pdfGenerator.generatePayslipPdf(model);
        log.info("Payslip PDF generated successfully for employeeId={} and period={}", employeeId, period);

        return pdfBytes;
    }
}
