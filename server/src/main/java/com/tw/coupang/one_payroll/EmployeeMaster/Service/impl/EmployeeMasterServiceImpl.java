package com.tw.coupang.one_payroll.EmployeeMaster.Service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tw.coupang.one_payroll.EmployeeMaster.Dto.CreateEmployeeRequest;
import com.tw.coupang.one_payroll.EmployeeMaster.Entity.EmployeeMaster;
import com.tw.coupang.one_payroll.EmployeeMaster.Enum.EmployeeStatus;
import com.tw.coupang.one_payroll.EmployeeMaster.Repository.EmployeeMasterRepository;
import com.tw.coupang.one_payroll.EmployeeMaster.Service.EmployeeMasterService;

import lombok.RequiredArgsConstructor;

@Service
public class EmployeeMasterServiceImpl implements EmployeeMasterService {

    private final EmployeeMasterRepository repository;

    public EmployeeMasterServiceImpl(EmployeeMasterRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public EmployeeMaster createEmployee(CreateEmployeeRequest request) {

        if (repository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already in use");
        }

        EmployeeMaster employee = EmployeeMaster.builder()
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

        return repository.save(employee);
    }
}

