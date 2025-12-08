package com.tw.coupang.one_payroll.timesheet.repository;

import com.tw.coupang.one_payroll.timesheet.entity.TimesheetSummary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TimesheetRepository extends JpaRepository<TimesheetSummary, Long> {

    Optional<TimesheetSummary> findByEmployeeIdAndPayPeriodId(String employeeId, Integer payPeriodId);
}
