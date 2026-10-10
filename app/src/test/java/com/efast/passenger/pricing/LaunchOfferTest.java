package com.efast.passenger.pricing;

import com.efast.passenger.data.model.FareQuote;
import com.efast.passenger.util.FareCalculator;

import org.junit.Test;

import java.time.LocalDate;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class LaunchOfferTest {

    private final LaunchOfferRules rules = LaunchOfferRules.defaults();
    private final LocalDate duringOffer = LocalDate.of(2026, 10, 9);

    /** The worked example in LaunchOffer's comment. */
    @Test
    public void workedExample_11250m() {
        LaunchOffer o = LaunchOffer.evaluate(FareCalculator.quote(11_250), rules, duringOffer);
        assertTrue(o.show);
        assertEquals(31_238, o.standardTotalPaise);
        assertEquals(23_035, o.actualTotalPaise);
        assertEquals(8_203, o.savingPaise);
        assertEquals(26, o.percentOff);
    }

    @Test
    public void longRide_standardBelowRs25PerKm_notShown() {
        // Rs 50 + 20 km x Rs 22 = Rs 490 -> Rs 24.50/km, not above Rs 25
        LaunchOffer o = LaunchOffer.evaluate(FareCalculator.quote(20_000), rules, duringOffer);
        assertFalse(o.show);
    }

    @Test
    public void exactlyRs25PerKm_notShown_onePaiseMore_shown() {
        // Rs 50 + X km x Rs 22 = Rs 25 x X  ->  X = 16.667 km. Use a flat Rs 25/km tariff to hit it exactly.
        LaunchOfferRules flat25 = new LaunchOfferRules(0, 2_500, 2_500, rules.lastDay);
        assertFalse(LaunchOffer.evaluate(FareCalculator.quote(10_000), flat25, duringOffer).show);

        LaunchOfferRules flat2501 = new LaunchOfferRules(0, 2_501, 2_500, rules.lastDay);
        assertTrue(LaunchOffer.evaluate(FareCalculator.quote(10_000), flat2501, duringOffer).show);
    }

    @Test
    public void percentIsRoundedDown_neverOverstated() {
        LaunchOffer o = LaunchOffer.evaluate(FareCalculator.quote(11_250), rules, duringOffer);
        double exact = 100.0 * o.savingPaise / o.standardTotalPaise; // 26.26...
        assertTrue(o.percentOff <= exact);
        assertTrue(o.percentOff > exact - 1);
    }

    @Test
    public void afterLastDay_notShown() {
        LaunchOffer last = LaunchOffer.evaluate(FareCalculator.quote(5_000), rules, rules.lastDay);
        LaunchOffer after = LaunchOffer.evaluate(FareCalculator.quote(5_000), rules, rules.lastDay.plusDays(1));
        assertTrue(last.show);
        assertFalse(after.show);
    }

    @Test
    public void standardNotDearerThanEfast_notShown() {
        LaunchOfferRules cheapStandard = new LaunchOfferRules(0, 1_900, 1_000, rules.lastDay);
        assertFalse(LaunchOffer.evaluate(FareCalculator.quote(10_000), cheapStandard, duringOffer).show);
    }

    @Test
    public void zeroDistance_notShown() {
        assertFalse(LaunchOffer.evaluate(new FareQuote(0, 0, 0, 0), rules, duringOffer).show);
    }
}
