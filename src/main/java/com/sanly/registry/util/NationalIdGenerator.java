package com.sanly.registry.util;

import com.sanly.registry.entity.Gender;

import java.time.LocalDate;

/**
 * Generates and validates Turkmenistan National Identification Numbers (TM-NIN).
 *
 * <pre>
 * Format (11 digits):
 *  Pos 1    : gender + century code  (1–6)
 *  Pos 2–3  : year   (YY, last 2 digits of birth year)
 *  Pos 4–5  : month  (MM)
 *  Pos 6–7  : day    (DD)
 *  Pos 8–10 : sequence number within that prefix (001–999)
 *  Pos 11   : check digit
 *
 * Gender/century codes:
 *  1 = Male   1800–1899   2 = Female 1800–1899
 *  3 = Male   1900–1999   4 = Female 1900–1999
 *  5 = Male   2000–2099   6 = Female 2000–2099
 *
 * Check digit algorithm:
 *  w1 = [1,2,3,4,5,6,7,8,9,1]  — applied to digits 1–10
 *  check = (Σ d[i]·w1[i]) mod 11
 *  If check == 10, use fallback weights w2 = [3,4,5,6,7,8,9,1,2,3]
 * </pre>
 */
public final class NationalIdGenerator {

    private static final int[] WEIGHTS_PRIMARY   = {1, 2, 3, 4, 5, 6, 7, 8, 9, 1};
    private static final int[] WEIGHTS_SECONDARY = {3, 4, 5, 6, 7, 8, 9, 1, 2, 3};

    private NationalIdGenerator() {}

    /**
     * Builds the 7-character prefix (gender/century code + YYMMDD) for the given
     * date of birth and gender. Used as the key in the nin_sequences table.
     */
    public static String buildPrefix(LocalDate dob, Gender gender) {
        int code = genderCenturyCode(dob.getYear(), gender);
        return String.format("%d%02d%02d%02d",
                code,
                dob.getYear() % 100,
                dob.getMonthValue(),
                dob.getDayOfMonth());
    }

    /**
     * Generates a complete 11-digit TM-NIN for the given parameters.
     *
     * @param dob      citizen's date of birth
     * @param gender   citizen's gender
     * @param sequence registration sequence for this date/gender (1–999)
     * @return 11-digit TM-NIN string
     */
    public static String generate(LocalDate dob, Gender gender, int sequence) {
        if (sequence < 1 || sequence > 999) {
            throw new IllegalArgumentException(
                    "Sequence must be 1–999, got: " + sequence);
        }
        String partial = buildPrefix(dob, gender) + String.format("%03d", sequence);
        return partial + computeCheckDigit(partial);
    }

    /**
     * Computes the check digit for a 10-digit partial NIN string.
     */
    public static int computeCheckDigit(String first10) {
        if (first10 == null || first10.length() != 10) {
            throw new IllegalArgumentException("Input must be exactly 10 digits");
        }
        int[] d = toDigits(first10, 10);
        int check = weightedSum(d, WEIGHTS_PRIMARY) % 11;
        if (check == 10) {
            check = weightedSum(d, WEIGHTS_SECONDARY) % 11;
        }
        return check;
    }

    // ---- private helpers ----

    private static int genderCenturyCode(int year, Gender gender) {
        boolean male = gender == Gender.MALE;
        if (year >= 1800 && year <= 1899) return male ? 1 : 2;
        if (year >= 1900 && year <= 1999) return male ? 3 : 4;
        if (year >= 2000 && year <= 2099) return male ? 5 : 6;
        throw new IllegalArgumentException(
                "Birth year " + year + " is outside supported range (1800–2099)");
    }

    private static int weightedSum(int[] digits, int[] weights) {
        int sum = 0;
        for (int i = 0; i < digits.length; i++) {
            sum += digits[i] * weights[i];
        }
        return sum;
    }

    static int[] toDigits(String s, int length) {
        int[] d = new int[length];
        for (int i = 0; i < length; i++) {
            d[i] = s.charAt(i) - '0';
        }
        return d;
    }
}
