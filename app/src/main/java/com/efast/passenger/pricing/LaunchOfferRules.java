package com.efast.passenger.pricing;

import java.time.LocalDate;

/**
 * Settings for the "Standard fare" vs launch-offer display on Fare Quote.
 *
 * IMPORTANT: the standard fare shown crossed out must be a real, comparable price
 * (for example EFast's own published standard tariff), not a made-up number. Under
 * India's Consumer Protection Act 2019 and the CCPA Guidelines for Prevention and
 * Regulation of Dark Patterns (2023), a fake "was" price is a misleading
 * advertisement. The defaults below are PLACEHOLDERS: replace them with EFast's
 * actual standard tariff before release, and keep {@link #lastDay} honest.
 */
public final class LaunchOfferRules {

    /** Standard tariff, before GST: flag-fall plus a per-km rate. */
    public final long standardBasePaise;
    public final long standardRatePaisePerKm;
    /** Show the crossed-out fare only when the standard fare per km is ABOVE this. */
    public final long thresholdPaisePerKm;
    /** Last day the launch offer is shown. After this the normal fare card returns. */
    public final LocalDate lastDay;

    public LaunchOfferRules(long standardBasePaise, long standardRatePaisePerKm,
                            long thresholdPaisePerKm, LocalDate lastDay) {
        this.standardBasePaise = standardBasePaise;
        this.standardRatePaisePerKm = standardRatePaisePerKm;
        this.thresholdPaisePerKm = thresholdPaisePerKm;
        this.lastDay = lastDay;
    }

    /** Placeholder standard tariff: Rs 50 + Rs 22/km (+ 5% GST). Threshold Rs 25/km. */
    public static LaunchOfferRules defaults() {
        return new LaunchOfferRules(5_000, 2_200, 2_500, LocalDate.of(2026, 12, 31));
    }
}
