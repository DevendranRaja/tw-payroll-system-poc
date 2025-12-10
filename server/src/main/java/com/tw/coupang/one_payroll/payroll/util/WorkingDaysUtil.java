package com.tw.coupang.one_payroll.payroll.util;

import java.time.DayOfWeek;
import java.time.LocalDate;

public class WorkingDaysUtil {
    public static int getWeekdaysBetween(LocalDate start, LocalDate end) {
        int count = 0;
        for (LocalDate date = start; !date.isAfter(end); date = date.plusDays(1)) {
            DayOfWeek dow = date.getDayOfWeek();
            if (dow != DayOfWeek.SATURDAY && dow != DayOfWeek.SUNDAY) {
                count++;
            }
        }
        return count;
    }
}
