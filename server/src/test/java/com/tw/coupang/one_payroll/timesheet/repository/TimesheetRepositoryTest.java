package com.tw.coupang.one_payroll.timesheet.repository;

import com.tw.coupang.one_payroll.timesheet.entity.TimesheetSummary;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@EntityScan(basePackageClasses = TimesheetSummary.class)
@EnableJpaRepositories(basePackageClasses = TimesheetRepository.class)
class TimesheetRepositoryTest {

    @Autowired
    private TimesheetRepository timesheetRepository;

    @Test
    void findByEmployeeIdAndPayPeriodIdFound() {
        TimesheetSummary ts = TimesheetSummary.builder()
                .employeeId("EMP1")
                .payPeriodId(100)
                .noOfDaysWorked(5)
                .hoursWorked(BigDecimal.TEN)
                .build();
        timesheetRepository.save(ts);

        Optional<TimesheetSummary> result = timesheetRepository.findByEmployeeIdAndPayPeriodId("EMP1", 100);

        assertTrue(result.isPresent());
        assertEquals("EMP1", result.get().getEmployeeId());
    }

    @Test
    void findByEmployeeIdAndPayPeriodIdNotFound() {
        Optional<TimesheetSummary> result = timesheetRepository.findByEmployeeIdAndPayPeriodId("NONEXISTENT", 999);
        assertTrue(result.isEmpty());
    }
}
