package com.tw.coupang.one_payroll.paygroups.service;

import com.tw.coupang.one_payroll.paygroups.dto.request.PayGroupCreateRequest;
import com.tw.coupang.one_payroll.paygroups.dto.request.PayGroupUpdateRequest;
import com.tw.coupang.one_payroll.paygroups.dto.response.PayGroupDetailsResponse;
import com.tw.coupang.one_payroll.paygroups.dto.response.PayGroupResponse;
import com.tw.coupang.one_payroll.paygroups.entity.PayGroup;
import com.tw.coupang.one_payroll.paygroups.enums.PaymentCycle;
import com.tw.coupang.one_payroll.paygroups.repository.PayGroupRepository;
import com.tw.coupang.one_payroll.paygroups.validator.PayGroupValidator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
public class PayGroupServiceImpl implements PayGroupService {

    private final PayGroupRepository payGroupRepository;
    private final PayGroupValidator payGroupValidator;

    public PayGroupServiceImpl(PayGroupRepository payGroupRepository, PayGroupValidator payGroupValidator) {
        this.payGroupRepository = payGroupRepository;
        this.payGroupValidator = payGroupValidator;
    }

    @Transactional
    @Override
    public PayGroupResponse create(PayGroupCreateRequest request) {
        String name = request.getGroupName().trim();
        log.info("Creating new pay group with name: {}", name);

        payGroupValidator.validateDuplicateName(name);
        log.debug("Duplicate name validation passed for: {}", name);

        PayGroup payGroup = buildPayGroup(request, name);
        PayGroup savedPayGroup = payGroupRepository.save(payGroup);

        log.info("Pay group '{}' created successfully with ID: {}", name, savedPayGroup.getId());
        return buildPayGroupResponse(savedPayGroup.getId());
    }

    @Transactional
    @Override
    public PayGroupResponse update(Integer id, PayGroupUpdateRequest request) {
        log.info("Updating pay group with ID: {}", id);

        PayGroup existing = payGroupValidator.validatePayGroupExists(id);
        log.debug("Existing pay group retrieved: {}", existing);

        String newName = request.getGroupName() != null ? request.getGroupName().trim() : null;
        if (newName != null) {
            payGroupValidator.validateDuplicateName(newName);
            log.debug("Duplicate name validation passed for new name: {}", newName);
        }

        PayGroup updated = PayGroup.builder()
                .id(existing.getId())
                .groupName(resolve(newName, existing.getGroupName()))
                .paymentCycle(resolve(request.getPaymentCycle(), existing.getPaymentCycle()))
                .baseTaxRate(resolve(request.getBaseTaxRate(), existing.getBaseTaxRate()))
                .benefitRate(resolve(request.getBenefitRate(), existing.getBenefitRate()))
                .deductionRate(resolve(request.getDeductionRate(), existing.getDeductionRate()))
                .createdAt(existing.getCreatedAt())
                .build();

        updated = payGroupRepository.save(updated);
        log.info("Pay group ID {} updated successfully", updated.getId());

        return buildPayGroupResponse(updated.getId());
    }

    @Transactional(readOnly = true)
    @Override
    public List<PayGroupDetailsResponse> getAll(PaymentCycle paymentCycle) {
        log.info("Fetching all pay groups. Filter paymentCycle={}", paymentCycle);

        List<PayGroup> groups =
                (paymentCycle != null)
                        ? payGroupRepository.findByPaymentCycle(paymentCycle)
                        : payGroupRepository.findAll();

        if (groups.isEmpty()) {
            log.info("No pay groups found, returning empty list.");
            return List.of();
        }

        return groups.stream()
                .map(this::mapToResponse)
                .toList();
    }

    private PayGroupDetailsResponse mapToResponse(PayGroup entity) {
        return PayGroupDetailsResponse.builder()
                .payGroupId(entity.getId())
                .groupName(entity.getGroupName())
                .paymentCycle(entity.getPaymentCycle())
                .baseTaxRate(entity.getBaseTaxRate())
                .benefitRate(entity.getBenefitRate())
                .deductionRate(entity.getDeductionRate())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    private PayGroup buildPayGroup(PayGroupCreateRequest request, String name) {
        return PayGroup.builder()
                .groupName(name)
                .paymentCycle(request.getPaymentCycle())
                .baseTaxRate(request.getBaseTaxRate())
                .benefitRate(request.getBenefitRate())
                .deductionRate(request.getDeductionRate())
                .build();
    }

    private PayGroupResponse buildPayGroupResponse(Integer payGroupId) {
        return PayGroupResponse.builder()
                .payGroupId(payGroupId)
                .build();
    }

    private <T> T resolve(T newValue, T oldValue) {
        return newValue != null ? newValue : oldValue;
    }
}
