package com.sanly.registry.util;

/**
 * Validates a Turkmenistan National Identification Number (TM-NIN).
 * Stateless — all methods are static.
 */
public final class NationalIdValidator {

    private static final int[] WEIGHTS_PRIMARY   = {1, 2, 3, 4, 5, 6, 7, 8, 9, 1};
    private static final int[] WEIGHTS_SECONDARY = {3, 4, 5, 6, 7, 8, 9, 1, 2, 3};

    private NationalIdValidator() {}

    /**
     * Returns {@code true} if {@code nin} is a structurally valid TM-NIN:
     * <ul>
     *   <li>Exactly 11 digits</li>
     *   <li>Digit 1 is 1–6 (valid gender/century code)</li>
     *   <li>Month portion (digits 4–5) is 01–12</li>
     *   <li>Day portion (digits 6–7) is 01–31</li>
     *   <li>Sequence portion (digits 8–10) is 001–999</li>
     *   <li>Check digit (digit 11) matches the algorithm</li>
     * </ul>
     */
    public static boolean isValid(String nin) {
        if (nin == null || nin.length() != 11 || !nin.matches("\\d{11}")) {
            return false;
        }

        int[] d = NationalIdGenerator.toDigits(nin, 11);

        if (d[0] < 1 || d[0] > 6) return false;

        int month = d[2] * 10 + d[3];
        if (month < 1 || month > 12) return false;

        int day = d[4] * 10 + d[5];
        if (day < 1 || day > 31) return false;

        int seq = d[7] * 100 + d[8] * 10 + d[9];
        if (seq < 1 || seq > 999) return false;

        return d[10] == expectedCheckDigit(d);
    }

    private static int expectedCheckDigit(int[] d) {
        int sum = 0;
        for (int i = 0; i < 10; i++) sum += d[i] * WEIGHTS_PRIMARY[i];
        int check = sum % 11;
        if (check == 10) {
            sum = 0;
            for (int i = 0; i < 10; i++) sum += d[i] * WEIGHTS_SECONDARY[i];
            check = sum % 11;
        }
        return check;
    }
}
