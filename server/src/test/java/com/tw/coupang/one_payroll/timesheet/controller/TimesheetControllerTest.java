package com.tw.coupang.one_payroll.timesheet.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.tw.coupang.one_payroll.timesheet.dto.TimesheetRequest;
import com.tw.coupang.one_payroll.timesheet.dto.TimesheetResponse;
import com.tw.coupang.one_payroll.timesheet.exception.InvalidTimesheetException;
import com.tw.coupang.one_payroll.timesheet.service.TimesheetService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;

import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class TimesheetControllerTest {

    private MockMvc mockMvc;

    @Mock
    private TimesheetService timesheetService;

    @InjectMocks
    private TimesheetController timesheetController;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(timesheetController)
                .build();

        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Test
    void addOrUpdateTimesheetReturnsTwoHundredWhenValid() throws Exception {
        TimesheetRequest request = new TimesheetRequest();
        request.setEmployeeId("E1");
        request.setPayPeriodId(1);
        request.setNoOfDaysWorked(5);
        request.setHoursWorked(BigDecimal.TEN);

        TimesheetResponse response = TimesheetResponse.builder()
                .employeeId("E1")
                .message("Timesheet created successfully")
                .build();

        when(timesheetService.addOrUpdateTimesheet(any(TimesheetRequest.class))).thenReturn(response);

        mockMvc.perform(post("/timesheet")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Timesheet created successfully"))
                .andExpect(jsonPath("$.employeeId").value("E1"));
    }

    @Test
    void addOrUpdateTimesheetShouldThrowExceptionWhenServiceFails() {
        TimesheetRequest request = new TimesheetRequest();
        request.setEmployeeId("E1");
        request.setPayPeriodId(1);
        request.setNoOfDaysWorked(5);
        request.setHoursWorked(BigDecimal.TEN);

        when(timesheetService.addOrUpdateTimesheet(any(TimesheetRequest.class)))
                .thenThrow(new InvalidTimesheetException("Validation failed"));

        assertThrows(Exception.class, () -> {
            mockMvc.perform(post("/timesheet")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)));
        });
    }
}
