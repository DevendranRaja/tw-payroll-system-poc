package com.tw.coupang.one_payroll.payslip.service;

import com.tw.coupang.one_payroll.payslip.dto.MonthlyPayslipSummaryDto;
import com.tw.coupang.one_payroll.payslip.dto.PayslipMetadataDTO;
import com.tw.coupang.one_payroll.payslip.dto.YtdSummaryForPdfDto;
import com.tw.coupang.one_payroll.payslip.util.PdfGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Month;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class PdfService {

    private final PdfGenerator pdfGenerator;
    private final PayslipService payslipService;
    private final YtdSummaryService ytdSummaryService;

    public byte[] generatePayslipPdf(String employeeId, String period) {
        log.info("Generating payslip PDF for employeeId={} and period={}", employeeId, period);

        PayslipMetadataDTO response = payslipService.generatePayslipMetadata(employeeId, period);

        Map<String, Object> model = new HashMap<>();
        model.put("payslip", response);

        byte[] pdfBytes = pdfGenerator.generatePayslipPdf(model);
        log.info("Payslip PDF generated successfully for employeeId={} and period={}", employeeId, period);

        return pdfBytes;
    }

    public byte[] generateYtdPdf(String employeeId, int year) {
        log.info("Generating YTD PDF for employeeId={} and year={}", employeeId, year);

        YtdSummaryForPdfDto response = ytdSummaryService.getYtdSummaryWithBreakdown(employeeId, year);

        List<MonthlyPayslipSummaryDto> fullMonthList = getFullMonthList(response.monthlyBreakdown(), year);

        Map<String, Object> model = new HashMap<>();
        model.put("ytd", response);
        model.put("monthlyList", fullMonthList);

        byte[] pdfBytes = pdfGenerator.generateYtdPdf(model);
        log.info("YTD PDF generated successfully for employeeId={} and year={}", employeeId, year);

        return pdfBytes;
    }

    private List<MonthlyPayslipSummaryDto> getFullMonthList(Map<String, MonthlyPayslipSummaryDto> monthlyBreakdown, int year) {
        List<MonthlyPayslipSummaryDto> fullList = new ArrayList<>();
        for (Month month : Month.values()) {
            MonthlyPayslipSummaryDto dto = monthlyBreakdown.get(month.name());
            if (dto == null) {
                dto = new MonthlyPayslipSummaryDto(
                        month.name(),
                        month.getValue(),
                        year,
                        BigDecimal.ZERO,
                        BigDecimal.ZERO,
                        BigDecimal.ZERO,
                        BigDecimal.ZERO
                );
            }
            fullList.add(dto);
        }
        return fullList;
    }
}
