package com.tw.coupang.one_payroll.payperiod.service;

import com.tw.coupang.one_payroll.paygroups.entity.PayGroup;
import com.tw.coupang.one_payroll.paygroups.enums.PaymentCycle;
import com.tw.coupang.one_payroll.paygroups.validator.PayGroupValidator;
import com.tw.coupang.one_payroll.payperiod.dto.request.PayPeriod;
import com.tw.coupang.one_payroll.payperiod.dto.request.PayPeriodCreateRequest;
import com.tw.coupang.one_payroll.payperiod.dto.response.PayPeriodResponse;
import com.tw.coupang.one_payroll.payperiod.exception.OverlappingPayPeriodException;
import com.tw.coupang.one_payroll.payperiod.repository.PayPeriodRepository;
import com.tw.coupang.one_payroll.payperiod.validator.PayPeriodCycleValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PayPeriodServiceImplTest {

    @Mock
    private PayPeriodRepository payPeriodRepository;

    @Mock
    private PayGroupValidator payGroupValidator;

    @Mock
    private PayPeriodCycleValidator calculatorValidator;

    @InjectMocks
    private PayPeriodServiceImpl payPeriodService;

    private PayGroup payGroup;

    @BeforeEach
    void setup() {
        payGroup = new PayGroup();
        payGroup.setId(1);
        payGroup.setPaymentCycle(PaymentCycle.MONTHLY);
    }

    @Test
    void createOverlappingPayPeriodShouldThrowException() {
        LocalDate start = LocalDate.of(2025, 10, 1);
        LocalDate end = LocalDate.of(2025, 10, 31);

        PayPeriodCreateRequest request = PayPeriodCreateRequest.builder()
                .payGroupId(payGroup.getId())
                .payPeriod(new PayPeriod(start, end))
                .build();

        when(payGroupValidator.validatePayGroupExists(payGroup.getId())).thenReturn(payGroup);
        when(payPeriodRepository.existsOverlappingPeriod(payGroup.getId(), start, end)).thenReturn(true);

        OverlappingPayPeriodException exception = assertThrows(OverlappingPayPeriodException.class,
                () -> payPeriodService.create(request));

        assertTrue(exception.getMessage().contains("overlaps with existing period"));
    }

    @Test
    void createInvalidPayGroupShouldPropagateException() {
        PayPeriodCreateRequest request = PayPeriodCreateRequest.builder()
                .payGroupId(999)
                .payPeriod(new PayPeriod(LocalDate.now(), LocalDate.now().plusDays(1)))
                .build();

        when(payGroupValidator.validatePayGroupExists(999)).thenThrow(new RuntimeException("Pay group not found"));

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> payPeriodService.create(request));

        assertEquals("Pay group not found", exception.getMessage());
    }

    @Test
    void createInvalidCycleShouldPropagateException() {
        LocalDate start = LocalDate.of(2025, 10, 1);
        LocalDate end = LocalDate.of(2025, 10, 31);

        PayPeriodCreateRequest request = PayPeriodCreateRequest.builder()
                .payGroupId(payGroup.getId())
                .payPeriod(new PayPeriod(start, end))
                .build();

        when(payGroupValidator.validatePayGroupExists(payGroup.getId())).thenReturn(payGroup);
        doThrow(new RuntimeException("Invalid cycle")).when(calculatorValidator).validatePayPeriodAgainstPayGroup(start, end, payGroup);

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> payPeriodService.create(request));

        assertEquals("Invalid cycle", exception.getMessage());
    }

    @Test
    void createValidMonthlyPayPeriodShouldReturnResponse() {
        LocalDate start = LocalDate.of(2025, 10, 1);
        LocalDate end = LocalDate.of(2025, 10, 31);

        PayPeriodCreateRequest request = PayPeriodCreateRequest.builder()
                .payGroupId(payGroup.getId())
                .payPeriod(new PayPeriod(start, end))
                .build();

        when(payGroupValidator.validatePayGroupExists(payGroup.getId())).thenReturn(payGroup);
        when(payPeriodRepository.existsOverlappingPeriod(payGroup.getId(), start, end)).thenReturn(false);

        com.tw.coupang.one_payroll.payperiod.entity.PayPeriod savedPeriod = com.tw.coupang.one_payroll.payperiod.entity.PayPeriod.builder()
                .id(100)
                .payGroupId(payGroup.getId())
                .periodStartDate(start)
                .periodEndDate(end)
                .range("OCT-2025")
                .build();

        when(payPeriodRepository.save(any(com.tw.coupang.one_payroll.payperiod.entity.PayPeriod.class))).thenReturn(savedPeriod);

        PayPeriodResponse response = payPeriodService.create(request);

        assertNotNull(response);
        assertEquals(100, response.id());

        ArgumentCaptor<com.tw.coupang.one_payroll.payperiod.entity.PayPeriod> captor = ArgumentCaptor.forClass(com.tw.coupang.one_payroll.payperiod.entity.PayPeriod.class);
        verify(payPeriodRepository).save(captor.capture());
        com.tw.coupang.one_payroll.payperiod.entity.PayPeriod captured = captor.getValue();
        assertEquals(payGroup.getId(), captured.getPayGroupId());
        assertEquals(start, captured.getPeriodStartDate());
        assertEquals(end, captured.getPeriodEndDate());
        assertEquals("OCT-2025", captured.getRange());

        verify(calculatorValidator).validatePayPeriodAgainstPayGroup(start, end, payGroup);
    }

    @Test
    void createValidWeeklyPayPeriodShouldReturnResponse() {
        LocalDate start = LocalDate.of(2025, 6, 1);
        LocalDate end = LocalDate.of(2025, 6, 7);

        payGroup.setPaymentCycle(PaymentCycle.WEEKLY);

        PayPeriodCreateRequest request = PayPeriodCreateRequest.builder()
                .payGroupId(payGroup.getId())
                .payPeriod(new PayPeriod(start, end))
                .build();

        when(payGroupValidator.validatePayGroupExists(payGroup.getId())).thenReturn(payGroup);
        when(payPeriodRepository.existsOverlappingPeriod(payGroup.getId(), start, end)).thenReturn(false);

        com.tw.coupang.one_payroll.payperiod.entity.PayPeriod savedPeriod = com.tw.coupang.one_payroll.payperiod.entity.PayPeriod.builder()
                .id(101)
                .payGroupId(payGroup.getId())
                .periodStartDate(start)
                .periodEndDate(end)
                .range("01-07 JUN25")
                .build();

        when(payPeriodRepository.save(any(com.tw.coupang.one_payroll.payperiod.entity.PayPeriod.class))).thenReturn(savedPeriod);

        PayPeriodResponse response = payPeriodService.create(request);

        assertNotNull(response);
        assertEquals(101, response.id());

        ArgumentCaptor<com.tw.coupang.one_payroll.payperiod.entity.PayPeriod> captor = ArgumentCaptor.forClass(com.tw.coupang.one_payroll.payperiod.entity.PayPeriod.class);
        verify(payPeriodRepository).save(captor.capture());
        com.tw.coupang.one_payroll.payperiod.entity.PayPeriod captured = captor.getValue();
        assertEquals(payGroup.getId(), captured.getPayGroupId());
        assertEquals(start, captured.getPeriodStartDate());
        assertEquals(end, captured.getPeriodEndDate());
        assertEquals("01-07 JUN25", captured.getRange());

        verify(calculatorValidator).validatePayPeriodAgainstPayGroup(start, end, payGroup);
    }

    @Test
    void createValidBiweeklyPayPeriodShouldReturnResponse() {
        LocalDate start = LocalDate.of(2025, 6, 8);
        LocalDate end = LocalDate.of(2025, 6, 21);

        payGroup.setPaymentCycle(PaymentCycle.BIWEEKLY);

        PayPeriodCreateRequest request = PayPeriodCreateRequest.builder()
                .payGroupId(payGroup.getId())
                .payPeriod(new PayPeriod(start, end))
                .build();

        when(payGroupValidator.validatePayGroupExists(payGroup.getId())).thenReturn(payGroup);
        when(payPeriodRepository.existsOverlappingPeriod(payGroup.getId(), start, end)).thenReturn(false);

        com.tw.coupang.one_payroll.payperiod.entity.PayPeriod savedPeriod = com.tw.coupang.one_payroll.payperiod.entity.PayPeriod.builder()
                .id(102)
                .payGroupId(payGroup.getId())
                .periodStartDate(start)
                .periodEndDate(end)
                .range("08-21 JUN25")
                .build();

        when(payPeriodRepository.save(any(com.tw.coupang.one_payroll.payperiod.entity.PayPeriod.class))).thenReturn(savedPeriod);

        PayPeriodResponse response = payPeriodService.create(request);

        assertNotNull(response);
        assertEquals(102, response.id());

        ArgumentCaptor<com.tw.coupang.one_payroll.payperiod.entity.PayPeriod> captor = ArgumentCaptor.forClass(com.tw.coupang.one_payroll.payperiod.entity.PayPeriod.class);
        verify(payPeriodRepository).save(captor.capture());
        com.tw.coupang.one_payroll.payperiod.entity.PayPeriod captured = captor.getValue();
        assertEquals(payGroup.getId(), captured.getPayGroupId());
        assertEquals(start, captured.getPeriodStartDate());
        assertEquals(end, captured.getPeriodEndDate());
        assertEquals("08-21 JUN25", captured.getRange());

        verify(calculatorValidator).validatePayPeriodAgainstPayGroup(start, end, payGroup);
    }
}
