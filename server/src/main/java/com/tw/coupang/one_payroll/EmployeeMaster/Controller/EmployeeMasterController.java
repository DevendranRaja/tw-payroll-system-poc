package com.tw.coupang.one_payroll.EmployeeMaster.Controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.tw.coupang.one_payroll.EmployeeMaster.Dto.CreateEmployeeRequest;
import com.tw.coupang.one_payroll.EmployeeMaster.Dto.UpdateEmployeeRequest;
import com.tw.coupang.one_payroll.EmployeeMaster.Entity.EmployeeMaster;
import com.tw.coupang.one_payroll.EmployeeMaster.Service.EmployeeMasterService;

import jakarta.validation.Valid;

import java.util.Map;

@RestController
@RequestMapping
@Validated
public class EmployeeMasterController {

    private final EmployeeMasterService employeeMasterService;

    public EmployeeMasterController(EmployeeMasterService employeeMasterService) {
        this.employeeMasterService = employeeMasterService;
    }

    @PostMapping("/employee")
    public ResponseEntity<?> createEmployee(@Valid @RequestBody CreateEmployeeRequest request) {
        try {
            EmployeeMaster created = employeeMasterService.createEmployee(request);
            return new ResponseEntity<>(created, HttpStatus.CREATED);
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred");
        }
    }

    @PutMapping("/updateEmployee/{employeeId}")
    public ResponseEntity<?> updateEmployee(@PathVariable String employeeId, @Valid @RequestBody UpdateEmployeeRequest request) {
        try {
            EmployeeMaster updated = employeeMasterService.updateEmployee(employeeId, request);
            return new ResponseEntity<>(updated, HttpStatus.OK);
        } catch (IllegalArgumentException ex) {
            String msg = ex.getMessage() == null ? "Bad request" : ex.getMessage();
            String lower = msg.toLowerCase();
            if (lower.contains("not found")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", msg));
            } else if (lower.contains("invalid status")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", msg));
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", msg));
            }
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "An unexpected error occurred"));
        }
    }

}
