package com.efast.passenger.util;

import com.efast.passenger.data.model.FareQuote;

import java.util.Locale;

/**
 * EFast fare = distance x Rs 19.50/km, plus 5% GST on that base fare.
 *
 * Worked example, 11.25 km (EFast's average paid trip):
 *   base  = 11,250 m x 1,950 paise/km / 1,000 = 21,937.5 -> rounds to 21,938 paise = Rs 219.38
 *   GST   = 21,938 x 5 / 100                   =  1,096.9 -> rounds to  1,097 paise = Rs  10.97
 *   total = 21,938 + 1,097                     = 23,035 paise                      = Rs 230.35
 */
public final class FareCalculator {

    /** Rs 19.50 per km, excluding GST. */
    public static final long RATE_PAISE_PER_KM = 1950;
    public static final long GST_PERCENT = 5;

    private FareCalculator() {
    }

    public static FareQuote quote(long distanceMeters) {
        long base = divideRoundHalfUp(distanceMeters * RATE_PAISE_PER_KM, 1000);
        long gst = divideRoundHalfUp(base * GST_PERCENT, 100);
        return new FareQuote(distanceMeters, base, gst, base + gst);
    }

    /** 23035 -> "₹230.35" */
    public static String rupees(long paise) {
        return String.format(Locale.ENGLISH, "₹%,d.%02d", paise / 100, paise % 100);
    }

    /** 11250 -> "11.3 km" */
    public static String km(long meters) {
        return String.format(Locale.ENGLISH, "%.1f km", meters / 1000.0);
    }

    public static long divideRoundHalfUp(long numerator, long denominator) {
        return (numerator + denominator / 2) / denominator;
    }
}
