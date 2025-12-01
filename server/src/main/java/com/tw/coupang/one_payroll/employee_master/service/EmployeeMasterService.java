package com.tw.coupang.one_payroll.employee_master.service;

import com.tw.coupang.one_payroll.employee_master.dto.CreateEmployeeRequest;
import com.tw.coupang.one_payroll.employee_master.dto.UpdateEmployeeRequest;
import com.tw.coupang.one_payroll.employee_master.entity.EmployeeMaster;

import java.util.List;

public interface EmployeeMasterService {
    EmployeeMaster createEmployee(CreateEmployeeRequest request);
    EmployeeMaster updateEmployee(String employeeId, UpdateEmployeeRequest request);
    EmployeeMaster getEmployeeById(String employeeId);
    List<EmployeeMaster> getEmployeesByDepartment(String department, boolean includeInactive);
    void deleteEmployee(String employeeId);
}
