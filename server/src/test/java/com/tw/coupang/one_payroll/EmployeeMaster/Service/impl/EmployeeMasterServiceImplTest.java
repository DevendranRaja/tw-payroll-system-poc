package com.tw.coupang.one_payroll.EmployeeMaster.Service.impl;

import com.tw.coupang.one_payroll.EmployeeMaster.Dto.CreateEmployeeRequest;
import com.tw.coupang.one_payroll.EmployeeMaster.Dto.UpdateEmployeeRequest;
import com.tw.coupang.one_payroll.EmployeeMaster.Entity.EmployeeMaster;
import com.tw.coupang.one_payroll.EmployeeMaster.Enum.EmployeeStatus;
import com.tw.coupang.one_payroll.EmployeeMaster.Exception.EmployeeConflictException;
import com.tw.coupang.one_payroll.EmployeeMaster.Repository.EmployeeMasterRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class EmployeeMasterServiceImplTest {

    @Mock
    private EmployeeMasterRepository repository;

    @InjectMocks
    private EmployeeMasterServiceImpl service;

    @SuppressWarnings("resource")
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createEmployee_success() {
        CreateEmployeeRequest request =  new CreateEmployeeRequest(
                "E001", "John", "Doe", "IT", "Developer", "john.doe@example.com", 1, LocalDate.now()
        );

        when(repository.existsByEmail(request.getEmail())).thenReturn(false);
        EmployeeMaster saved = EmployeeMaster.builder()
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
        when(repository.save(any(EmployeeMaster.class))).thenReturn(saved);

        EmployeeMaster result = service.createEmployee(request);
        assertNotNull(result);
        assertEquals("E001", result.getEmployeeId());
        assertEquals(EmployeeStatus.ACTIVE, result.getStatus());
        verify(repository, times(1)).save(any(EmployeeMaster.class));
    }

    @Test
    void createEmployee_employeeIdAlreadyExists_throwsConflictException() {
        CreateEmployeeRequest request =  new CreateEmployeeRequest(
                "E002", "Jane", "Smith", "HR", "Manager", "jane.smith@example.com", 2, LocalDate.now()
        );

        when(repository.existsByEmployeeId(request.getEmployeeId())).thenReturn(true);

        EmployeeConflictException exception = assertThrows(EmployeeConflictException.class, () -> service.createEmployee(request));
        assertEquals("Employee ID already exists", exception.getMessage());
        verify(repository, never()).save(any(EmployeeMaster.class));
    }

    @Test
    void createEmployee_emailAlreadyExists_throwsConflictException() {
        CreateEmployeeRequest request =  new CreateEmployeeRequest(
                "E003", "Alice", "Brown", "Finance", "Analyst", "alice.brown@example.com", 3, LocalDate.now()
        );

        when(repository.existsByEmployeeId(request.getEmployeeId())).thenReturn(false);
        when(repository.existsByEmail(request.getEmail())).thenReturn(true);

        EmployeeConflictException exception = assertThrows(EmployeeConflictException.class, () -> service.createEmployee(request));
        assertEquals("Email already in use", exception.getMessage());
        verify(repository, never()).save(any(EmployeeMaster.class));
    }

    @Test
    void updateEmployee_success_withStatusChange() {
        String empId = "E001";
        UpdateEmployeeRequest update = new UpdateEmployeeRequest("John", "DoeUpdated", "IT", "Senior Dev", "john.doe@example.com", 1, LocalDate.now(), "INACTIVE");

        EmployeeMaster existing = EmployeeMaster.builder()
                .employeeId(empId)
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .payGroupId(1)
                .status(EmployeeStatus.ACTIVE)
                .build();

        when(repository.findById(empId)).thenReturn(Optional.of(existing));
        when(repository.existsByEmail(update.getEmail())).thenReturn(false);
        EmployeeMaster saved = EmployeeMaster.builder()
                .employeeId(empId)
                .firstName(update.getFirstName())
                .lastName(update.getLastName())
                .email(update.getEmail())
                .payGroupId(update.getPayGroupId())
                .status(EmployeeStatus.INACTIVE)
                .build();
        when(repository.save(any(EmployeeMaster.class))).thenReturn(saved);

        EmployeeMaster result = service.updateEmployee(empId, update);
        assertNotNull(result);
        assertEquals(EmployeeStatus.INACTIVE, result.getStatus());
        assertEquals(update.getLastName(), result.getLastName());
    }

    @Test
    void updateEmployee_notFound_throwsException() {
        String empId = "E999";
        UpdateEmployeeRequest update = new UpdateEmployeeRequest("Non", "Exist", "Dept", "Role", "non.exist@example.com", 1, LocalDate.now(), null);
        when(repository.findById(empId)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> service.updateEmployee(empId, update));
    }

    @Test
    void getEmployeeById_success() {
        String empId = "E010";
        EmployeeMaster existing = EmployeeMaster.builder()
                .employeeId(empId)
                .firstName("Sam")
                .lastName("Lee")
                .email("sam.lee@example.com")
                .payGroupId(2)
                .status(EmployeeStatus.ACTIVE)
                .build();

        when(repository.findById(empId)).thenReturn(Optional.of(existing));

        EmployeeMaster result = service.getEmployeeById(empId);

        assertNotNull(result);
        assertEquals(empId, result.getEmployeeId());
    }

    @Test
    void getEmployeeById_notFound_throws() {
        String empId = "E404";
        when(repository.findById(empId)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> service.getEmployeeById(empId));
    }

    @Test
    void getEmployeesByDepartment_returnsActiveOnly_whenIncludeInactiveFalse() {
        String dept = "Finance";
        EmployeeMaster a = EmployeeMaster.builder().employeeId("E201").department(dept).status(EmployeeStatus.ACTIVE).build();
        EmployeeMaster b = EmployeeMaster.builder().employeeId("E202").department(dept).status(EmployeeStatus.INACTIVE).build();

        when(repository.findByDepartmentAndStatus(dept, EmployeeStatus.ACTIVE)).thenReturn(Arrays.asList(a));

        java.util.List<EmployeeMaster> result = service.getEmployeesByDepartment(dept, false);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(EmployeeStatus.ACTIVE, result.get(0).getStatus());
    }

    @Test
    void getEmployeesByDepartment_returnsAll_whenIncludeInactiveTrue() {
        String dept = "Finance";
        EmployeeMaster a = EmployeeMaster.builder().employeeId("E201").department(dept).status(EmployeeStatus.ACTIVE).build();
        EmployeeMaster b = EmployeeMaster.builder().employeeId("E202").department(dept).status(EmployeeStatus.INACTIVE).build();

        when(repository.findByDepartment(dept)).thenReturn(Arrays.asList(a, b));

        java.util.List<EmployeeMaster> result = service.getEmployeesByDepartment(dept, true);

        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    void getEmployeesByDepartment_emptyWhenNoDeptProvided() {
        java.util.List<EmployeeMaster> result = service.getEmployeesByDepartment(null, false);
        assertTrue(result.isEmpty());
    }
}
