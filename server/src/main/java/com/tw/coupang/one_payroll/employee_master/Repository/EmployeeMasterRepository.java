package com.tw.coupang.one_payroll.employee_master.Repository;

import com.tw.coupang.one_payroll.employee_master.Entity.EmployeeMaster;
import com.tw.coupang.one_payroll.employee_master.Enum.EmployeeStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmployeeMasterRepository extends JpaRepository<EmployeeMaster, String> {
    boolean existsByEmail(String email);
    boolean existsByEmployeeId(String employeeId);
    List<EmployeeMaster> findByDepartmentIgnoreCase(String department);
    List<EmployeeMaster> findByDepartmentIgnoreCaseAndStatus(String department, EmployeeStatus status);
}
