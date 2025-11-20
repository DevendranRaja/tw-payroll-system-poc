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

    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<?> getEmployeeById(@PathVariable String employeeId) {
        EmployeeMaster employee = employeeMasterService.getEmployeeById(employeeId);
        return ResponseEntity.ok(employee);
    }

    @GetMapping("/employees")
    public ResponseEntity<?> getEmployeesByDepartment(
            @RequestParam(value = "department", required = false) String department,
            @RequestParam(value = "includeInactive", required = false, defaultValue = "false") boolean includeInactive) {

        if (includeInactive && (department == null || department.trim().isEmpty())) {
            throw new IllegalArgumentException("includeInactive parameter requires department to be specified");
        }

        if (department == null || department.trim().isEmpty()) {
            throw new IllegalArgumentException("department query parameter is required");
        }

        List<EmployeeMaster> list = employeeMasterService.getEmployeesByDepartment(department, includeInactive);

        if (list == null || list.isEmpty()) {
            throw new IllegalArgumentException("No employees found for department: " + department);
        }

        return ResponseEntity.ok(list);
    }

}
