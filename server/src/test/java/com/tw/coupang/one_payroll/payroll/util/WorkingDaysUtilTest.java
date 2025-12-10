package com.tw.coupang.one_payroll.payroll.util;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static com.tw.coupang.one_payroll.payroll.util.WorkingDaysUtil.getWeekdaysBetween;
import static org.junit.jupiter.api.Assertions.assertEquals;

class WorkingDaysUtilTest {

    @Test
    void shouldReturnCorrectWeekdaysForWeeklyCycle() {
        LocalDate start = LocalDate.of(2025, 12, 1);
        LocalDate end = LocalDate.of(2025, 12, 7);

        int weekdays = getWeekdaysBetween(start, end);

        assertEquals(5, weekdays);
    }

    @Test
    void shouldReturnZeroWhenAllDaysAreWeekends() {
        LocalDate start = LocalDate.of(2025, 12, 6);
        LocalDate end = LocalDate.of(2025, 12, 7);

        int weekdays = getWeekdaysBetween(start, end);

        assertEquals(0, weekdays);
    }

    @Test
    void shouldReturnOneWhenStartEqualsEndAndIsWeekday() {
        LocalDate date = LocalDate.of(2025, 12, 3); // Wednesday

        int weekdays = getWeekdaysBetween(date, date);

        assertEquals(1, weekdays);
    }

    @Test
    void shouldReturnZeroWhenStartEqualsEndAndIsWeekend() {
        LocalDate date = LocalDate.of(2025, 12, 6);

        int weekdays = getWeekdaysBetween(date, date);

        assertEquals(0, weekdays);
    }

    @Test
    void shouldHandleCrossMonthRanges() {
        LocalDate start = LocalDate.of(2025, 11, 28);
        LocalDate end = LocalDate.of(2025, 12, 3);

        int weekdays = getWeekdaysBetween(start, end);

        assertEquals(4, weekdays);
    }
}
