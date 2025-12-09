package com.tw.coupang.one_payroll.timesheet.controller;

import com.tw.coupang.one_payroll.timesheet.dto.TimesheetRequest;
import com.tw.coupang.one_payroll.timesheet.dto.TimesheetResponse;
import com.tw.coupang.one_payroll.timesheet.service.TimesheetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/timesheet")
@RequiredArgsConstructor
public class TimesheetController {

    private final TimesheetService timesheetService;

    @PostMapping
    public ResponseEntity<TimesheetResponse> addOrUpdateTimesheet(@Valid @RequestBody TimesheetRequest request) {
        TimesheetResponse response = timesheetService.addOrUpdateTimesheet(request);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
