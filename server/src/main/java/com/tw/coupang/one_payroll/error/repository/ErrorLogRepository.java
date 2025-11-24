package com.tw.coupang.one_payroll.error.repository;

import com.tw.coupang.one_payroll.error.entity.ErrorLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ErrorLogRepository extends JpaRepository<ErrorLog, Long> {
    List<ErrorLog> findByEmployeeEmployeeId(String employeeId);
    List<ErrorLog> findByModuleName(String moduleName);
}
