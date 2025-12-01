package com.tw.coupang.one_payroll.employee_master.Service;

import com.tw.coupang.one_payroll.employee_master.Dto.CreateEmployeeRequest;
import com.tw.coupang.one_payroll.employee_master.Dto.UpdateEmployeeRequest;
import com.tw.coupang.one_payroll.employee_master.Entity.EmployeeMaster;

import java.util.List;

public interface EmployeeMasterService {
    EmployeeMaster createEmployee(CreateEmployeeRequest request);
    EmployeeMaster updateEmployee(String employeeId, UpdateEmployeeRequest request);
    EmployeeMaster getEmployeeById(String employeeId);
    List<EmployeeMaster> getEmployeesByDepartment(String department, boolean includeInactive);
    void deleteEmployee(String employeeId);
}
