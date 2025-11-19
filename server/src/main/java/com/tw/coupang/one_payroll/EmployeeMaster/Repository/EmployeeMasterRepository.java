package com.tw.coupang.one_payroll.EmployeeMaster.Repository;

import com.tw.coupang.one_payroll.EmployeeMaster.Entity.EmployeeMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EmployeeMasterRepository extends JpaRepository<EmployeeMaster, String> {
    boolean existsByEmail(String email);
    boolean existsByEmployeeId(String employeeId);
}

