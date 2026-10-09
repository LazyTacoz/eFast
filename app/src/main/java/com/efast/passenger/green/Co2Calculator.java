package com.efast.passenger.green;

import java.util.Locale;

/**
 * CO2 for one ride, in whole grams. Worked out in milligrams first so short rides
 * don't round away to nothing.
 *
 * Worked example, 11.25 km with the India defaults:
 *   petrol car = 11,250 m x 2,310 g/L / 15,000 m/L      = 1,732.5 g
 *   EFast EV   = 11.25 km x 150 Wh/km x 716 g/kWh / 1000 = 1,208.25 g
 *   saved      = 1,732.5 - 1,208.25 = 524.25 g -> 524 g ("0.52 kg")
 */
public final class Co2Calculator {

    private final EmissionFactors factors;

    public Co2Calculator(EmissionFactors factors) {
        this.factors = factors;
    }

    public EmissionFactors factors() {
        return factors;
    }

    public long petrolCarGrams(long distanceMeters) {
        return roundMgToGrams(petrolCarMg(distanceMeters));
    }

    public long evGrams(long distanceMeters) {
        return roundMgToGrams(evMg(distanceMeters));
    }

    /** Never negative: if the EV ever came out dirtier, we claim no saving rather than a fake one. */
    public long savedGrams(long distanceMeters) {
        return roundMgToGrams(Math.max(0, petrolCarMg(distanceMeters) - evMg(distanceMeters)));
    }

    private long petrolCarMg(long meters) {
        if (meters <= 0) return 0;
        return meters * factors.petrolGramsCo2PerLitre * 1000 / factors.petrolCarMetersPerLitre;
    }

    private long evMg(long meters) {
        if (meters <= 0) return 0;
        // meters x Wh/km x g/kWh = mg x 1000, since (m / 1000) km and Wh / 1000 = kWh
        return meters * factors.evWattHoursPerKm * factors.gridGramsCo2PerKwh / 1000;
    }

    private static long roundMgToGrams(long mg) {
        return (mg + 500) / 1000;
    }

    /** 524 -> "0.52 kg", 12_340 -> "12.3 kg", 1_234_000 -> "1,234 kg" */
    public static String kg(long grams) {
        double kg = grams / 1000.0;
        if (grams < 10_000) return String.format(Locale.ENGLISH, "%.2f kg", kg);
        if (grams < 1_000_000) return String.format(Locale.ENGLISH, "%.1f kg", kg);
        return String.format(Locale.ENGLISH, "%,d kg", Math.round(kg));
    }

    /** Plain number for CSV: 524 -> "0.524" */
    public static String kgPlain(long grams) {
        return String.format(Locale.ENGLISH, "%d.%03d", grams / 1000, grams % 1000);
    }
}
