package com.tw.coupang.one_payroll.EmployeeMaster.Repository;

import com.tw.coupang.one_payroll.EmployeeMaster.Entity.EmployeeMaster;
import com.tw.coupang.one_payroll.EmployeeMaster.Enum.EmployeeStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmployeeMasterRepository extends JpaRepository<EmployeeMaster, String> {
    boolean existsByEmail(String email);
    boolean existsByEmployeeId(String employeeId);
    List<EmployeeMaster> findByDepartment(String department);
    List<EmployeeMaster> findByDepartmentAndStatus(String department, EmployeeStatus status);
}
