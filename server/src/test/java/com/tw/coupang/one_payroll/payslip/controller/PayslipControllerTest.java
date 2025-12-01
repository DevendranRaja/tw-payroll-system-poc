package com.tw.coupang.one_payroll.payslip.controller;

import com.tw.coupang.one_payroll.payslip.dto.PayslipResponse;
import com.tw.coupang.one_payroll.payslip.dto.YtdSummaryResponse;
import com.tw.coupang.one_payroll.payslip.service.PayslipService;
import com.tw.coupang.one_payroll.payslip.service.PdfService;
import com.tw.coupang.one_payroll.payslip.service.YtdSummaryService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class PayslipControllerTest {

    private MockMvc mockMvc;

    @Mock
    private PayslipService payslipService;

    @Mock
    private PdfService pdfService;

    @Mock
    private YtdSummaryService ytdSummaryService;

    private AutoCloseable mocks;

    @BeforeEach
    void setUp() {
        mocks = MockitoAnnotations.openMocks(this);
        PaySlipController controller =
                new PaySlipController(payslipService, pdfService, ytdSummaryService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @AfterEach
    void tearDown() throws Exception {
        if (mocks != null) mocks.close();
    }

    @Test
    void getPayslipSuccess() throws Exception {
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

        mockMvc.perform(get("/payslip-ess/{employeeId}/payslip", empId)
                        .param("period", period))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.employeeId").value(empId))
                .andExpect(jsonPath("$.period").value(period))
                .andExpect(jsonPath("$.grossPay").value(5000))
                .andExpect(jsonPath("$.netPay").value(4500));

        verify(payslipService, times(1)).getPayslipMetadata(empId, period);
    }

    @Test
    void downloadPayslipSuccess() throws Exception {
        String empId = "E001";
        String period = "2025-11";
        byte[] pdfData = new byte[]{1, 2, 3, 4};

        when(pdfService.generatePayslipPdf(empId, period)).thenReturn(pdfData);

        mockMvc.perform(get("/payslip-ess/{employeeId}/payslip/download", empId)
                        .param("period", period))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=payslip_" + period + ".pdf"))
                .andExpect(content().contentType(MediaType.APPLICATION_PDF))
                .andExpect(content().bytes(pdfData));

        verify(pdfService, times(1)).generatePayslipPdf(empId, period);
    }

    @Test
    void getYtdSummarySuccess() throws Exception {
        String empId = "E001";
        int year = 2025;

        YtdSummaryResponse response = new YtdSummaryResponse(
                BigDecimal.valueOf(16500),
                BigDecimal.valueOf(14900),
                BigDecimal.valueOf(1300),
                BigDecimal.valueOf(300)
        );

        when(ytdSummaryService.getYtdSummaryDetails(empId, year)).thenReturn(response);

        mockMvc.perform(get("/payslip-ess/{employeeId}/ytd-summary", empId)
                        .param("year", String.valueOf(year)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.totalGross").value(16500))
                .andExpect(jsonPath("$.totalNet").value(14900))
                .andExpect(jsonPath("$.totalDeductions").value(1300))
                .andExpect(jsonPath("$.totalBenefit").value(300));

        verify(ytdSummaryService, times(1)).getYtdSummaryDetails(empId, year);
    }

    @Test
    void downloadYtdPdfSuccess() throws Exception {
        String empId = "E001";
        int year = 2025;
        byte[] pdfData = new byte[]{5, 6, 7, 8};

        when(pdfService.generateYtdPdf(empId, year)).thenReturn(pdfData);

        mockMvc.perform(get("/payslip-ess/ytd/{employeeId}/{year}/download", empId, year))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=ytd_" + year + ".pdf"))
                .andExpect(content().contentType(MediaType.APPLICATION_PDF))
                .andExpect(content().bytes(pdfData));

        verify(pdfService, times(1)).generateYtdPdf(empId, year);
    }
}
