package com.tw.coupang.one_payroll.paygroups.service;

import com.tw.coupang.one_payroll.paygroups.dto.request.PayGroupCreateRequest;
import com.tw.coupang.one_payroll.paygroups.dto.request.PayGroupUpdateRequest;
import com.tw.coupang.one_payroll.paygroups.dto.response.PayGroupDetailsResponse;
import com.tw.coupang.one_payroll.paygroups.dto.response.PayGroupResponse;
import com.tw.coupang.one_payroll.paygroups.entity.PayGroup;
import com.tw.coupang.one_payroll.paygroups.enums.PaymentCycle;
import com.tw.coupang.one_payroll.paygroups.exception.DuplicatePayGroupException;
import com.tw.coupang.one_payroll.paygroups.repository.PayGroupRepository;
import com.tw.coupang.one_payroll.paygroups.validator.PayGroupValidator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
class PayGroupServiceImplTest {

    @Mock
    private PayGroupRepository payGroupRepository;

    @Mock
    private PayGroupValidator payGroupValidator;

    @InjectMocks
    private PayGroupServiceImpl payGroupService;

    @Test
    void createShouldSaveAndReturnResponse() {
        PayGroupCreateRequest request = buildCreateRequest();

        PayGroup expected = PayGroup.builder()
                .id(1)
                .groupName("Engineering")
                .paymentCycle(PaymentCycle.MONTHLY)
                .baseTaxRate(request.getBaseTaxRate())
                .benefitRate(request.getBenefitRate())
                .deductionRate(request.getDeductionRate())
                .createdAt(LocalDateTime.now())
                .build();

        doNothing().when(payGroupValidator).validateDuplicateName("Engineering");
        when(payGroupRepository.save(any(PayGroup.class))).thenReturn(expected);

        PayGroupResponse actual = payGroupService.create(request);

        assertThat(actual).isNotNull();
        assertThat(actual.getPayGroupId()).isEqualTo(expected.getId());

        verify(payGroupValidator).validateDuplicateName("Engineering");
        verify(payGroupRepository).save(any(PayGroup.class));
    }

    @Test
    void createShouldThrowDuplicatePayGroupExceptionWhenGroupNameAlreadyExists() {
        PayGroupCreateRequest request = buildCreateRequest();

        doThrow(new DuplicatePayGroupException("Pay group with name 'Engineering' already exists!"))
                .when(payGroupValidator).validateDuplicateName("Engineering");

        DuplicatePayGroupException ex = assertThrows(
                DuplicatePayGroupException.class,
                () -> payGroupService.create(request)
        );

        assertThat(ex.getMessage())
                .isEqualTo("Pay group with name 'Engineering' already exists!");

        verify(payGroupValidator).validateDuplicateName("Engineering");
        verify(payGroupRepository, never()).save(any());
    }

    @Test
    void createShouldThrowExceptionWhenGroupNameIsNull() {
        PayGroupCreateRequest request = PayGroupCreateRequest.builder()
                .groupName(null)
                .paymentCycle(PaymentCycle.WEEKLY)
                .baseTaxRate(BigDecimal.TEN)
                .benefitRate(BigDecimal.ONE)
                .deductionRate(BigDecimal.ONE)
                .build();

        assertThrows(NullPointerException.class, () -> payGroupService.create(request));
    }

    @Test
    void updateShouldUpdateAndReturnResponse() {
        PayGroupUpdateRequest request = PayGroupUpdateRequest.builder()
                .groupName("Engineering-New")
                .paymentCycle(PaymentCycle.BIWEEKLY)
                .baseTaxRate(BigDecimal.valueOf(12))
                .benefitRate(BigDecimal.valueOf(6))
                .deductionRate(BigDecimal.valueOf(3))
                .build();

        PayGroup existing = PayGroup.builder()
                .id(1)
                .groupName("Engineering")
                .paymentCycle(PaymentCycle.MONTHLY)
                .baseTaxRate(BigDecimal.TEN)
                .benefitRate(BigDecimal.ONE)
                .deductionRate(BigDecimal.ONE)
                .createdAt(LocalDateTime.now())
                .build();

        when(payGroupValidator.validatePayGroupExists(1)).thenReturn(existing);
        doNothing().when(payGroupValidator).validateDuplicateName("Engineering-New");

        PayGroup updated = PayGroup.builder()
                .id(1)
                .groupName("Engineering-New")
                .paymentCycle(PaymentCycle.BIWEEKLY)
                .baseTaxRate(BigDecimal.valueOf(12))
                .benefitRate(BigDecimal.valueOf(6))
                .deductionRate(BigDecimal.valueOf(3))
                .createdAt(existing.getCreatedAt())
                .build();

        when(payGroupRepository.save(any())).thenReturn(updated);

        PayGroupResponse response = payGroupService.update(1, request);

        assertThat(response).isNotNull();
        assertThat(response.getPayGroupId()).isEqualTo(1);

        verify(payGroupValidator).validatePayGroupExists(1);
        verify(payGroupValidator).validateDuplicateName("Engineering-New");
        verify(payGroupRepository).save(any());
    }

    @Test
    void updateShouldThrowDuplicatePayGroupExceptionWhenNewNameExists() {
        PayGroupUpdateRequest request = PayGroupUpdateRequest.builder()
                .groupName("Engineering")
                .build();

        PayGroup existing = PayGroup.builder()
                .id(1)
                .groupName("Ops")
                .createdAt(LocalDateTime.now())
                .build();

        when(payGroupValidator.validatePayGroupExists(1)).thenReturn(existing);

        doThrow(new DuplicatePayGroupException("Pay group with name 'Engineering' already exists!"))
                .when(payGroupValidator).validateDuplicateName("Engineering");

        assertThrows(DuplicatePayGroupException.class,
                () -> payGroupService.update(1, request));

        verify(payGroupValidator).validateDuplicateName("Engineering");
        verify(payGroupRepository, never()).save(any());
    }

    @Test
    void getAllShouldReturnAllPayGroupsWhenNoFilterApplied() {
        PayGroup pg1 = PayGroup.builder()
                .id(1)
                .groupName("Engineering")
                .paymentCycle(PaymentCycle.MONTHLY)
                .baseTaxRate(BigDecimal.TEN)
                .benefitRate(BigDecimal.ONE)
                .deductionRate(BigDecimal.ONE)
                .createdAt(LocalDateTime.now())
                .build();

        PayGroup pg2 = PayGroup.builder()
                .id(2)
                .groupName("Ops")
                .paymentCycle(PaymentCycle.WEEKLY)
                .baseTaxRate(BigDecimal.TEN)
                .benefitRate(BigDecimal.ONE)
                .deductionRate(BigDecimal.ONE)
                .createdAt(LocalDateTime.now())
                .build();

        when(payGroupRepository.findAll()).thenReturn(List.of(pg1, pg2));

        List<PayGroupDetailsResponse> response = payGroupService.getAll(null);

        assertThat(response).hasSize(2);
        assertThat(response.get(0).getPayGroupId()).isEqualTo(1);
        assertThat(response.get(1).getPayGroupId()).isEqualTo(2);

        verify(payGroupRepository).findAll();
        verify(payGroupRepository, never()).findByPaymentCycle(any());
    }

    @Test
    void getAllShouldReturnFilteredGroupsWhenPaymentCycleProvided() {
        PayGroup pg = PayGroup.builder()
                .id(1)
                .groupName("Engineering")
                .paymentCycle(PaymentCycle.MONTHLY)
                .baseTaxRate(BigDecimal.TEN)
                .benefitRate(BigDecimal.ONE)
                .deductionRate(BigDecimal.ONE)
                .createdAt(LocalDateTime.now())
                .build();

        when(payGroupRepository.findByPaymentCycle(PaymentCycle.MONTHLY))
                .thenReturn(List.of(pg));

        List<PayGroupDetailsResponse> response = payGroupService.getAll(PaymentCycle.MONTHLY);

        assertThat(response).hasSize(1);
        assertThat(response.get(0).getPaymentCycle()).isEqualTo(PaymentCycle.MONTHLY);

        verify(payGroupRepository).findByPaymentCycle(PaymentCycle.MONTHLY);
        verify(payGroupRepository, never()).findAll();
    }

    @Test
    void getAllShouldReturnEmptyListWhenNoPayGroupsFound() {
        when(payGroupRepository.findAll()).thenReturn(List.of());

        List<PayGroupDetailsResponse> response = payGroupService.getAll(null);

        assertThat(response).isEmpty();

        verify(payGroupRepository).findAll();
    }

    private PayGroupCreateRequest buildCreateRequest() {
        return PayGroupCreateRequest.builder()
                .groupName("Engineering")
                .paymentCycle(PaymentCycle.MONTHLY)
                .baseTaxRate(BigDecimal.valueOf(10.00))
                .benefitRate(BigDecimal.valueOf(5.00))
                .deductionRate(BigDecimal.valueOf(2.50))
                .build();
    }
}
