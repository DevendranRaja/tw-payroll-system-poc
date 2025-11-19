package com.tw.coupang.one_payroll.paygroups.validator;

import com.tw.coupang.one_payroll.paygroups.entity.PayGroup;
import com.tw.coupang.one_payroll.paygroups.exception.DuplicatePayGroupException;
import com.tw.coupang.one_payroll.paygroups.exception.PayGroupNotFoundException;
import com.tw.coupang.one_payroll.paygroups.repository.PayGroupRepository;
import org.springframework.stereotype.Component;

@Component
public class PayGroupValidator {
    private final PayGroupRepository repository;

    public PayGroupValidator(PayGroupRepository repository) {
        this.repository = repository;
    }

    public void validateDuplicateName(String name) {
        if (repository.existsByGroupNameIgnoreCase(name)) {
            throw new DuplicatePayGroupException("Pay group with name '" + name + "' already exists!");
        }
    }

    public PayGroup validatePayGroupExists(Integer id) {
        return repository.findById(id)
                .orElseThrow(() -> new PayGroupNotFoundException("Pay group with ID '" + id + "' not found!"));
    }
}
