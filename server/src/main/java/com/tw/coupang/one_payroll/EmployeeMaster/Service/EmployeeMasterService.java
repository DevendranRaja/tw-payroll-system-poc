package com.tw.coupang.one_payroll.EmployeeMaster.Service;

import com.tw.coupang.one_payroll.EmployeeMaster.Dto.CreateEmployeeRequest;
import com.tw.coupang.one_payroll.EmployeeMaster.Dto.UpdateEmployeeRequest;
import com.tw.coupang.one_payroll.EmployeeMaster.Entity.EmployeeMaster;

import java.util.List;

public interface EmployeeMasterService {
    EmployeeMaster createEmployee(CreateEmployeeRequest request);
    EmployeeMaster updateEmployee(String employeeId, UpdateEmployeeRequest request);
    EmployeeMaster getEmployeeById(String employeeId);
    List<EmployeeMaster> getEmployeesByDepartment(String department, boolean includeInactive);
}
