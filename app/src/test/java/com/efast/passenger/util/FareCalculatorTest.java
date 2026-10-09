package com.efast.passenger.util;

import com.efast.passenger.data.model.FareQuote;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Verifies FareCalculator against the spec's worked example and rounding edge cases.
 */
public class FareCalculatorTest {

    /**
     * Spec example: 11,250 m -> base 21,938 paise (Rs 219.38),
     * GST 1,097 paise (Rs 10.97), total 23,035 paise (Rs 230.35).
     */
    @Test
    public void specExample_11250m() {
        FareQuote q = FareCalculator.quote(11_250);
        assertEquals(11_250, q.distanceMeters);
        assertEquals(21_938, q.baseFarePaise);
        assertEquals(1_097, q.gstPaise);
        assertEquals(23_035, q.totalPaise);
    }

    @Test
    public void rupees_format() {
        assertEquals("₹230.35", FareCalculator.rupees(23_035));
        assertEquals("₹0.00", FareCalculator.rupees(0));
        assertEquals("₹1.00", FareCalculator.rupees(100));
        assertEquals("₹0.01", FareCalculator.rupees(1));
    }

    @Test
    public void km_format() {
        assertEquals("11.3 km", FareCalculator.km(11_250));
        assertEquals("0.0 km", FareCalculator.km(0));
        assertEquals("1.0 km", FareCalculator.km(1_000));
    }

    @Test
    public void roundHalfUp_exactHalf() {
        // 5 / 2 = 2.5 -> rounds to 3
        assertEquals(3, FareCalculator.divideRoundHalfUp(5, 2));
    }

    @Test
    public void roundHalfUp_belowHalf() {
        // 4 / 3 = 1.333... -> rounds to 1
        assertEquals(1, FareCalculator.divideRoundHalfUp(4, 3));
    }

    @Test
    public void roundHalfUp_aboveHalf() {
        // 5 / 3 = 1.666... -> rounds to 2
        assertEquals(2, FareCalculator.divideRoundHalfUp(5, 3));
    }

    @Test
    public void zeroDistance() {
        FareQuote q = FareCalculator.quote(0);
        assertEquals(0, q.distanceMeters);
        assertEquals(0, q.baseFarePaise);
        assertEquals(0, q.gstPaise);
        assertEquals(0, q.totalPaise);
    }

    @Test
    public void oneKm() {
        FareQuote q = FareCalculator.quote(1_000);
        // base = 1000 * 1950 / 1000 = 1950 (exact)
        assertEquals(1_950, q.baseFarePaise);
        // gst = 1950 * 5 / 100 = 97.5 -> rounds to 98
        assertEquals(98, q.gstPaise);
        assertEquals(2_048, q.totalPaise);
    }
}
