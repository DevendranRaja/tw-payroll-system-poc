package com.tw.coupang.one_payroll.paygroups.validator;

import com.tw.coupang.one_payroll.paygroups.entity.PayGroup;
import com.tw.coupang.one_payroll.paygroups.exception.DuplicatePayGroupException;
import com.tw.coupang.one_payroll.paygroups.exception.PayGroupNotFoundException;
import com.tw.coupang.one_payroll.paygroups.repository.PayGroupRepository;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PayGroupValidatorTest {

    private final PayGroupRepository repository = mock(PayGroupRepository.class);
    private final PayGroupValidator validator = new PayGroupValidator(repository);

    @Test
    void validateDuplicateName_whenNameExists_shouldThrowException() {
        when(repository.existsByGroupNameIgnoreCase("HR")).thenReturn(true);

        assertThrows(DuplicatePayGroupException.class,
                () -> validator.validateDuplicateName("HR"));
    }

    @Test
    void validateDuplicateName_whenNameUnique_shouldNotThrowException() {
        when(repository.existsByGroupNameIgnoreCase("Finance")).thenReturn(false);

        assertDoesNotThrow(() -> validator.validateDuplicateName("Finance"));
    }

    @Test
    void validatePayGroupExists_whenFound_shouldReturnEntity() {
        PayGroup payGroup = PayGroup.builder().id(1).groupName("HR").build();

        when(repository.findById(1)).thenReturn(Optional.of(payGroup));

        PayGroup result = validator.validatePayGroupExists(1);

        assertEquals("HR", result.getGroupName());
        assertEquals(1, result.getId());
    }

    @Test
    void validatePayGroupExists_whenNotFound_shouldThrowException() {
        when(repository.findById(99)).thenReturn(Optional.empty());

        assertThrows(PayGroupNotFoundException.class,
                () -> validator.validatePayGroupExists(99));
    }
}
