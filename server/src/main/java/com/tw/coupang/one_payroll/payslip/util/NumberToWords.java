package com.tw.coupang.one_payroll.payslip.util;

import java.text.DecimalFormat;

public class NumberToWords {

    private static final String[] TENS_NAMES = {
            "", " ten", " twenty", " thirty", " forty", " fifty",
            " sixty", " seventy", " eighty", " ninety"
    };

    private static final String[] NUM_NAMES = {
            "", " one", " two", " three", " four", " five",
            " six", " seven", " eight", " nine", " ten",
            " eleven", " twelve", " thirteen", " fourteen", " fifteen",
            " sixteen", " seventeen", " eighteen", " nineteen"
    };

    private static String convertLessThanOneThousand(int number) {
        String current;

        if (number % 100 < 20) {
            current = NUM_NAMES[number % 100];
            number /= 100;
        } else {
            current = NUM_NAMES[number % 10];
            number /= 10;

            current = TENS_NAMES[number % 10] + current;
            number /= 10;
        }

        if (number == 0) return current.trim();
        return NUM_NAMES[number] + " hundred" + current;
    }

    public static String convert(long number) {
        if (number == 0) {
            return "Zero";
        }

        String mask = "000000000000";
        DecimalFormat df = new DecimalFormat(mask);
        String snumber = df.format(number);

        int billions = Integer.parseInt(snumber.substring(0, 3));
        int millions = Integer.parseInt(snumber.substring(3, 6));
        int hundredThousands = Integer.parseInt(snumber.substring(6, 9));
        int thousands = Integer.parseInt(snumber.substring(9, 12));

        String tradBillions =
                (billions == 0) ? "" : convertLessThanOneThousand(billions) + " billion ";
        String tradMillions =
                (millions == 0) ? "" : convertLessThanOneThousand(millions) + " million ";
        String tradHundredThousands =
                (hundredThousands == 0) ? "" : convertLessThanOneThousand(hundredThousands) + " thousand ";
        String tradThousands =
                convertLessThanOneThousand(thousands);

        String result = (tradBillions + tradMillions + tradHundredThousands + tradThousands)
                .trim()
                .replaceAll("\\s+", " ");

        return capitalizeFirst(result);
    }

    private static String capitalizeFirst(String input) {
        if (input == null || input.isEmpty()) return input;
        return Character.toUpperCase(input.charAt(0)) + input.substring(1);
    }

}
