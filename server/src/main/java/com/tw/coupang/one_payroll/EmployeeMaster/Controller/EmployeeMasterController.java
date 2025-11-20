package com.tw.coupang.one_payroll.EmployeeMaster.Controller;

import com.tw.coupang.one_payroll.EmployeeMaster.Dto.CreateEmployeeRequest;
import com.tw.coupang.one_payroll.EmployeeMaster.Dto.UpdateEmployeeRequest;
import com.tw.coupang.one_payroll.EmployeeMaster.Entity.EmployeeMaster;
import com.tw.coupang.one_payroll.EmployeeMaster.Service.EmployeeMasterService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

import java.util.List;


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
        EmployeeMaster created = employeeMasterService.createEmployee(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/employee/{employeeId}")
    public ResponseEntity<?> updateEmployee(@PathVariable String employeeId,
                                            @Valid @RequestBody UpdateEmployeeRequest request) {
        EmployeeMaster updated = employeeMasterService.updateEmployee(employeeId, request);
        return ResponseEntity.ok(updated);
    }

    @GetMapping("/employees")
    public ResponseEntity<?> retrieveEmployees(
            @RequestParam(value = "employeeId", required = false) String employeeId,
            @RequestParam(value = "department", required = false) String department,
            @RequestParam(value = "includeInactive", required = false, defaultValue = "false") boolean includeInactive) {

        if (employeeId != null && !employeeId.trim().isEmpty()) {
            EmployeeMaster employee = employeeMasterService.getEmployeeById(employeeId.trim());
            return ResponseEntity.ok(employee);
        }

        List<EmployeeMaster> list = employeeMasterService.getEmployeesByDepartment(department, includeInactive);
        return ResponseEntity.ok(list);
    }

}
