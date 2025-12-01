package com.tw.coupang.one_payroll.payslip.controller;

import com.tw.coupang.one_payroll.payslipEss.dto.PayslipResponse;
import com.tw.coupang.one_payroll.payslipEss.dto.YtdSummaryResponse;
import com.tw.coupang.one_payroll.payslipEss.service.PayslipService;
import com.tw.coupang.one_payroll.payslipEss.service.PdfService;
import com.tw.coupang.one_payroll.payslipEss.service.YtdSummaryService;
import com.tw.coupang.one_payroll.payslip.dto.PayslipResponse;
import com.tw.coupang.one_payroll.payslip.service.PayslipService;
import com.tw.coupang.one_payroll.payslip.service.PdfService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/payslip-ess")
@RequiredArgsConstructor
@Slf4j
public class PaySlipController {

    private final PayslipService payslipService;
    private final PdfService pdfService;
    private final YtdSummaryService ytdSummaryService;

    @GetMapping("/{employeeId}/payslip")
    public PayslipResponse getPayslip(
            @PathVariable String employeeId,
            @RequestParam("period") String period
    ) {
        log.info("Fetching payslip for employeeId={}, period={}", employeeId, period);

        return payslipService.getPayslipMetadata(employeeId, period);
    }

    @GetMapping("/{employeeId}/payslip/download")
    public ResponseEntity<byte[]> downloadPayslip(
            @PathVariable String employeeId,
            @RequestParam("period") String period
    ) {
        log.info("Downloading payslip PDF for employeeId={}, period={}", employeeId, period);

        byte[] pdf = pdfService.generatePayslipPdf(employeeId, period);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=payslip_" + period + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    @GetMapping("/{employeeId}/ytd-summary")
    public YtdSummaryResponse getYtdSummary(
            @PathVariable String employeeId,
            @RequestParam("year") int year
    ) {
        log.info("Fetching YTD summary for employeeId={}, year={}", employeeId, year);
        return ytdSummaryService.getYtdSummaryDetails(employeeId, year);
    }


    @GetMapping("/ytd/{employeeId}/{year}/download")
    public ResponseEntity<byte[]> downloadYtdPdf(
            @PathVariable String employeeId,
            @PathVariable int year
    ) {
        log.info("Downloading YTD PDF for employeeId={}, year={}", employeeId, year);

        byte[] pdf = pdfService.generateYtdPdf(employeeId, year);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=ytd_" + year + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

}
