package com.tw.coupang.one_payroll.payslip.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class NumberToWordsTest {

    @Test
    void testZero() {
        assertEquals("Zero", NumberToWords.convert(0));
    }

    @Test
    void testSingleDigits() {
        assertEquals("One", NumberToWords.convert(1));
        assertEquals("Nine", NumberToWords.convert(9));
    }

    @Test
    void testTens() {
        assertEquals("Twenty one", NumberToWords.convert(21));
        assertEquals("Ninety nine", NumberToWords.convert(99));
    }

    @Test
    void testHundreds() {
        assertEquals("One hundred one", NumberToWords.convert(101));
        assertEquals("Five hundred sixty seven", NumberToWords.convert(567));
    }

    @Test
    void testThousands() {
        assertEquals("One thousand one", NumberToWords.convert(1001));
        assertEquals("Twelve thousand three hundred forty five", NumberToWords.convert(12345));
    }

    @Test
    void testMillions() {
        assertEquals("Two million three hundred forty five thousand six hundred seventy eight",
                NumberToWords.convert(2345678L));
    }

    @Test
    void testBillions() {
        assertEquals("One billion two hundred thirty four million five hundred sixty seven thousand eight hundred ninety",
                NumberToWords.convert(1234567890L));
    }

    @Test
    void testLargeNumber() {
        assertEquals("Nine hundred ninety nine billion nine hundred ninety nine million nine hundred ninety nine thousand nine hundred ninety nine",
                NumberToWords.convert(999_999_999_999L));
    }
}
