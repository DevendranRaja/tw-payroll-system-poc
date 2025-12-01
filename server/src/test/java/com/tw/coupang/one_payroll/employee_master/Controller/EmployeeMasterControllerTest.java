package com.tw.coupang.one_payroll.employee_master.Controller;

import com.tw.coupang.one_payroll.employee_master.Dto.CreateEmployeeRequest;
import com.tw.coupang.one_payroll.employee_master.Dto.UpdateEmployeeRequest;
import com.tw.coupang.one_payroll.employee_master.Entity.EmployeeMaster;
import com.tw.coupang.one_payroll.employee_master.Enum.EmployeeStatus;
import com.tw.coupang.one_payroll.employee_master.exception.EmployeeConflictException;
import com.tw.coupang.one_payroll.employee_master.exception.EmployeeNotFoundException;
import com.tw.coupang.one_payroll.employee_master.Service.EmployeeMasterService;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class EmployeeMasterControllerTest {

    @Mock
    private EmployeeMasterService employeeMasterService;

    @InjectMocks
    private EmployeeMasterController controller;

    // hold the AutoCloseable returned by MockitoAnnotations.openMocks to close it later
    private AutoCloseable mocks;

    @BeforeEach
    void setUp() {
        mocks = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void tearDown() throws Exception {
        if (mocks != null) {
            mocks.close();
        }
    }

    /** -------------------------------------------------
     * CREATE EMPLOYEE TESTS
     * ------------------------------------------------- */

    @Test
    void createEmployeeSuccess() {
        CreateEmployeeRequest request = new CreateEmployeeRequest(
                "E003", "Alice", "Brown", "Finance", "Analyst",
                "alice.brown@example.com", 3, LocalDate.now()
        );

        EmployeeMaster created = EmployeeMaster.builder()
                .employeeId(request.getEmployeeId())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .department(request.getDepartment())
                .designation(request.getDesignation())
                .email(request.getEmail())
                .payGroupId(request.getPayGroupId())
                .status(EmployeeStatus.ACTIVE)
                .joiningDate(request.getJoiningDate())
                .build();

        when(employeeMasterService.createEmployee(request)).thenReturn(created);

        ResponseEntity<?> response = controller.createEmployee(request);

        assertEquals(201, response.getStatusCode().value());
        assertEquals(created, response.getBody());
        verify(employeeMasterService, times(1)).createEmployee(request);
    }

    @Test
    void createEmployeeConflictExceptionPropagatesToGlobalHandler() {
        CreateEmployeeRequest request = new CreateEmployeeRequest(
                "E004", "Bob", "Johnson", "Sales", "Executive",
                "bob.johnson@example.com", 4, LocalDate.now()
        );

        when(employeeMasterService.createEmployee(request))
                .thenThrow(new EmployeeConflictException("Employee ID already exists"));

        // Controller does NOT handle the exception → GlobalExceptionHandler will
        assertThrows(EmployeeConflictException.class,
                () -> controller.createEmployee(request));

        verify(employeeMasterService, times(1)).createEmployee(request);
    }

    /** -------------------------------------------------
     * UPDATE EMPLOYEE TESTS
     * ------------------------------------------------- */

    @Test
    void updateEmployeeSuccess() {
        String empId = "E003";
        UpdateEmployeeRequest update = new UpdateEmployeeRequest(
                "Alice", "BrownUpdated", "Finance", "Sr Analyst",
                "alice.brown@example.com", 3, LocalDate.now(), "INACTIVE"
        );

        EmployeeMaster updated = EmployeeMaster.builder()
                .employeeId(empId)
                .firstName(update.getFirstName())
                .lastName(update.getLastName())
                .email(update.getEmail())
                .payGroupId(update.getPayGroupId())
                .status(EmployeeStatus.INACTIVE)
                .build();

        when(employeeMasterService.updateEmployee(empId, update)).thenReturn(updated);

        ResponseEntity<?> response = controller.updateEmployee(empId, update);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(updated, response.getBody());
    }

    @Test
    void updateEmployeeNotFoundPropagatesToGlobalHandler() {
        String empId = "E404";
        UpdateEmployeeRequest update = new UpdateEmployeeRequest(
                "Non", "Exist", "Dept", "Role",
                "non.exist@example.com", 1, LocalDate.now(), null
        );

        when(employeeMasterService.updateEmployee(empId, update))
                .thenThrow(new EmployeeNotFoundException("Employee with ID '" + empId + "' not found"));

        assertThrows(EmployeeNotFoundException.class,
                () -> controller.updateEmployee(empId, update));
    }

    @Test
    void updateEmployeePartialUpdateFirstNameOnly() {
        String empId = "E003";
        UpdateEmployeeRequest update = new UpdateEmployeeRequest();
        update.setFirstName("Alice_Updated");

        EmployeeMaster updated = EmployeeMaster.builder()
                .employeeId(empId)
                .firstName("Alice_Updated")
                .lastName("Brown")
                .email("alice.brown@example.com")
                .payGroupId(3)
                .status(EmployeeStatus.ACTIVE)
                .build();

        when(employeeMasterService.updateEmployee(empId, update)).thenReturn(updated);

        ResponseEntity<?> response = controller.updateEmployee(empId, update);

        assertEquals(200, response.getStatusCode().value());
        assertEquals("Alice_Updated",
                ((EmployeeMaster) response.getBody()).getFirstName());
    }

    @Test
    void updateEmployeePartialUpdateEmailOnly() {
        String empId = "E003";

        UpdateEmployeeRequest update = new UpdateEmployeeRequest();
        update.setEmail("newemail@example.com");

        EmployeeMaster updated = EmployeeMaster.builder()
                .employeeId(empId)
                .firstName("Alice")
                .lastName("Brown")
                .email("newemail@example.com")
                .payGroupId(3)
                .status(EmployeeStatus.ACTIVE)
                .build();

        when(employeeMasterService.updateEmployee(empId, update)).thenReturn(updated);

        ResponseEntity<?> response = controller.updateEmployee(empId, update);

        assertEquals(200, response.getStatusCode().value());
        assertEquals("newemail@example.com",
                ((EmployeeMaster) response.getBody()).getEmail());
    }

    @Test
    void getEmployeeByIdSuccess() {
        String empId = "E010";
        EmployeeMaster existing = EmployeeMaster.builder()
                .employeeId(empId)
                .firstName("Sam")
                .lastName("Lee")
                .email("sam.lee@example.com")
                .payGroupId(2)
                .status(EmployeeStatus.ACTIVE)
                .build();

        when(employeeMasterService.getEmployeeById(empId)).thenReturn(existing);

        ResponseEntity<?> response = controller.getEmployeeById(empId);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(existing, response.getBody());
    }

    @Test
    void getEmployeeByIdNotFound() {
        String empId = "E404";
        when(employeeMasterService.getEmployeeById(empId)).thenThrow(new EmployeeNotFoundException("Employee with ID '" + empId + "' not found"));

        assertThrows(EmployeeNotFoundException.class, () -> controller.getEmployeeById(empId));
    }

    @Test
    void getEmployeesByDepartmentSuccessActiveOnly() {
        String dept = "HR";
        EmployeeMaster a = EmployeeMaster.builder().employeeId("E101").department(dept).status(EmployeeStatus.ACTIVE).build();
        EmployeeMaster b = EmployeeMaster.builder().employeeId("E102").department(dept).status(EmployeeStatus.ACTIVE).build();

        when(employeeMasterService.getEmployeesByDepartment(dept, false)).thenReturn(Arrays.asList(a, b));

        ResponseEntity<?> response = controller.getEmployeesByDepartment(dept, false);

        assertEquals(200, response.getStatusCode().value());
        assertTrue(((java.util.List) response.getBody()).size() == 2);
    }

    @Test
    void getEmployeesByDepartmentSuccessIncludeInactive() {
        String dept = "Finance";
        EmployeeMaster a = EmployeeMaster.builder().employeeId("E201").department(dept).status(EmployeeStatus.ACTIVE).build();
        EmployeeMaster b = EmployeeMaster.builder().employeeId("E202").department(dept).status(EmployeeStatus.INACTIVE).build();

        when(employeeMasterService.getEmployeesByDepartment(dept, true)).thenReturn(Arrays.asList(a, b));

        ResponseEntity<?> response = controller.getEmployeesByDepartment(dept, true);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(2, ((java.util.List) response.getBody()).size());
    }

    @Test
    void getEmployeesByDepartmentMissingDepartmentThrows() {
        assertThrows(IllegalArgumentException.class, () -> controller.getEmployeesByDepartment(null, false));
    }

    @Test
    void getEmployeesByDepartmentIncludeInactiveWithoutDeptThrows() {
        assertThrows(IllegalArgumentException.class, () -> controller.getEmployeesByDepartment(null, true));
    }

    @Test
    void getEmployeesByDepartmentNoResultsThrows() {
        String dept = "NonDept";
        when(employeeMasterService.getEmployeesByDepartment(dept, false)).thenReturn(Collections.emptyList());

        assertThrows(IllegalArgumentException.class, () -> controller.getEmployeesByDepartment(dept, false));
    }

    @Test
    void deleteEmployeeSuccessMarksInactive() {
        String empId = "E100";

        doNothing().when(employeeMasterService).deleteEmployee(empId);

        ResponseEntity<?> response = controller.deleteEmployee(empId);
        assertEquals(204, response.getStatusCode().value());
        assertNull(response.getBody());
        verify(employeeMasterService, times(1)).deleteEmployee(empId);
    }

    @Test
    void deleteEmployeeNotFoundPropagatesToGlobalHandler() {
        String empId = "E404";
        doThrow(new EmployeeNotFoundException("Employee with ID '" + empId + "' not found")).when(employeeMasterService).deleteEmployee(empId);

        assertThrows(EmployeeNotFoundException.class, () -> controller.deleteEmployee(empId));
    }
}
