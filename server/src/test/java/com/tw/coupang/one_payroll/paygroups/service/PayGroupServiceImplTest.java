package com.tw.coupang.one_payroll.paygroups.service;

import com.tw.coupang.one_payroll.paygroups.dto.request.PayGroupCreateRequest;
import com.tw.coupang.one_payroll.paygroups.dto.response.PayGroupResponse;
import com.tw.coupang.one_payroll.paygroups.entity.PayGroup;
import com.tw.coupang.one_payroll.paygroups.enums.PaymentCycle;
import com.tw.coupang.one_payroll.paygroups.exception.DuplicatePayGroupException;
import com.tw.coupang.one_payroll.paygroups.repository.PayGroupRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
class PayGroupServiceImplTest {

    @Mock
    private PayGroupRepository payGroupRepository;

    @InjectMocks
    private PayGroupServiceImpl payGroupService;

    @Test
    void create_ShouldSaveAndReturnResponse() {
        PayGroupCreateRequest request = validRequest();

        PayGroup expected = PayGroup.builder()
                .id(1)
                .groupName("Engineering")
                .paymentCycle(PaymentCycle.MONTHLY)
                .baseTaxRate(request.getBaseTaxRate())
                .benefitRate(request.getBenefitRate())
                .deductionRate(request.getDeductionRate())
                .createdAt(LocalDateTime.now())
                .build();

        when(payGroupRepository.existsByGroupNameIgnoreCase("Engineering")).thenReturn(false);
        when(payGroupRepository.save(any(PayGroup.class))).thenReturn(expected);

        PayGroupResponse actual = payGroupService.create(request);

        assertThat(actual).isNotNull();
        assertThat(actual.getPayGroupId()).isEqualTo(expected.getId());
    }

    @Test
    void create_ShouldThrowDuplicatePayGroupException_WhenGroupNameAlreadyExists() {
        PayGroupCreateRequest request = validRequest();

        when(payGroupRepository.existsByGroupNameIgnoreCase("Engineering")).thenReturn(true);

        DuplicatePayGroupException ex = assertThrows(
                DuplicatePayGroupException.class,
                () -> payGroupService.create(request)
        );

        assertThat(ex.getMessage()).isEqualTo("Pay group with name 'Engineering' already exists!");
        verify(payGroupRepository, times(1)).existsByGroupNameIgnoreCase("Engineering");
        verify(payGroupRepository, never()).save(any());
    }

    @Test
    void create_ShouldThrowException_WhenGroupNameIsNull() {
        PayGroupCreateRequest request = PayGroupCreateRequest.builder()
                .groupName(null)
                .paymentCycle(PaymentCycle.WEEKLY)
                .baseTaxRate(BigDecimal.TEN)
                .benefitRate(BigDecimal.ONE)
                .deductionRate(BigDecimal.ONE)
                .build();

        assertThrows(NullPointerException.class, () -> payGroupService.create(request));
    }

    private PayGroupCreateRequest validRequest() {
        return PayGroupCreateRequest.builder()
                .groupName("Engineering")
                .paymentCycle(PaymentCycle.MONTHLY)
                .baseTaxRate(BigDecimal.valueOf(10.00))
                .benefitRate(BigDecimal.valueOf(5.00))
                .deductionRate(BigDecimal.valueOf(2.50))
                .build();
    }
}
