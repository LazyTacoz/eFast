package com.efast.passenger.pricing;

import com.efast.passenger.data.model.FareQuote;
import com.efast.passenger.util.FareCalculator;

import java.time.LocalDate;

/**
 * Decides whether Fare Quote shows "standard fare, crossed out" above EFast's real fare,
 * and works out the percentage off from the actual numbers (never hardcoded).
 *
 * Worked example, 11.25 km with the default rules:
 *   standard before GST = Rs 50 + 11.25 km x Rs 22 = Rs 297.50 -> Rs 26.44/km, above Rs 25 -> show
 *   standard total      = Rs 297.50 + 5% GST (Rs 14.88)   = Rs 312.38
 *   EFast total         = Rs 230.35
 *   percent off         = (312.38 - 230.35) / 312.38 = 26.26% -> shown as "26% off" (rounded down)
 */
public final class LaunchOffer {

    public final boolean show;
    public final long standardTotalPaise;
    public final long actualTotalPaise;
    public final long savingPaise;
    /** Rounded DOWN, so we never claim a bigger discount than the real one. */
    public final int percentOff;

    private LaunchOffer(boolean show, long standardTotalPaise, long actualTotalPaise) {
        this.show = show;
        this.standardTotalPaise = standardTotalPaise;
        this.actualTotalPaise = actualTotalPaise;
        this.savingPaise = Math.max(0, standardTotalPaise - actualTotalPaise);
        this.percentOff = standardTotalPaise <= 0 ? 0 : (int) (savingPaise * 100 / standardTotalPaise);
    }

    public static LaunchOffer evaluate(FareQuote actual, LaunchOfferRules rules, LocalDate today) {
        long meters = actual.distanceMeters;
        if (meters <= 0) return new LaunchOffer(false, 0, actual.totalPaise);

        long standardBase = standardBeforeGstPaise(meters, rules);
        long standardTotal = standardBase
                + FareCalculator.divideRoundHalfUp(standardBase * FareCalculator.GST_PERCENT, 100);

        // standardBase / km > threshold, without dividing: standardBase x 1000 > threshold x meters
        boolean aboveThreshold = standardBase * 1000 > rules.thresholdPaisePerKm * meters;
        boolean offerRunning = !today.isAfter(rules.lastDay);
        boolean cheaper = standardTotal > actual.totalPaise;

        LaunchOffer offer = new LaunchOffer(aboveThreshold && offerRunning && cheaper,
                standardTotal, actual.totalPaise);
        // "0% off" would look silly; treat it as no offer.
        if (offer.show && offer.percentOff < 1) {
            return new LaunchOffer(false, standardTotal, actual.totalPaise);
        }
        return offer;
    }

    static long standardBeforeGstPaise(long meters, LaunchOfferRules rules) {
        return rules.standardBasePaise
                + FareCalculator.divideRoundHalfUp(meters * rules.standardRatePaisePerKm, 1000);
    }
}
