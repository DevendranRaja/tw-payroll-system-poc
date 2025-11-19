package com.tw.coupang.one_payroll.paygroups.validator;

import com.tw.coupang.one_payroll.paygroups.entity.PayGroup;
import com.tw.coupang.one_payroll.paygroups.exception.DuplicatePayGroupException;
import com.tw.coupang.one_payroll.paygroups.exception.PayGroupNotFoundException;
import com.tw.coupang.one_payroll.paygroups.repository.PayGroupRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class PayGroupValidator {
    private final PayGroupRepository repository;

    public PayGroupValidator(PayGroupRepository repository) {
        this.repository = repository;
    }

    public void validateDuplicateName(String name) {
        log.debug("Validating duplicate name for pay group: {}", name);

        if (repository.existsByGroupNameIgnoreCase(name)) {
            log.warn("Duplicate pay group name detected: {}", name);
            throw new DuplicatePayGroupException("Pay group with name '" + name + "' already exists!");
        }

        log.debug("No duplicate found for pay group name: {}", name);
    }

    public PayGroup validatePayGroupExists(Integer id) {
        log.debug("Validating existence of pay group with ID: {}", id);

        return repository.findById(id)
                .orElseThrow(() -> new PayGroupNotFoundException("Pay group with ID '" + id + "' not found!"));
    }
}
