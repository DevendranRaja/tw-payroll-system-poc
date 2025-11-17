package com.tw.coupang.one_payroll.EmployeeMaster.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tw.coupang.one_payroll.EmployeeMaster.Dto.CreateEmployeeRequest;
import com.tw.coupang.one_payroll.EmployeeMaster.Entity.EmployeeMaster;
import com.tw.coupang.one_payroll.EmployeeMaster.Service.EmployeeMasterService;

import jakarta.validation.Valid;

@RestController
@RequestMapping
@Validated
public class EmployeeMasterController {

    private final EmployeeMasterService employeeMasterService;

    public EmployeeMasterController(EmployeeMasterService employeeMasterService) {
        this.employeeMasterService = employeeMasterService;
    }

    @PostMapping("/createEmployee")
    public ResponseEntity<?> createEmployee(@Valid @RequestBody CreateEmployeeRequest request) {
        try {
            EmployeeMaster created = employeeMasterService.createEmployee(request);
            return new ResponseEntity<>(created, HttpStatus.CREATED);
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred");
        }
    }

}
