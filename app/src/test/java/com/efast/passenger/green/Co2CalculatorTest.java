package com.efast.passenger.green;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class Co2CalculatorTest {

    private final Co2Calculator calc = new Co2Calculator(EmissionFactors.indiaDefaults());

    /** The worked example in Co2Calculator's comment. */
    @Test
    public void workedExample_11250m() {
        assertEquals(1_733, calc.petrolCarGrams(11_250)); // 1,732.5 g rounds up
        assertEquals(1_208, calc.evGrams(11_250));        // 1,208.25 g
        assertEquals(524, calc.savedGrams(11_250));       // 524.25 g, not 1,733 - 1,208 = 525
    }

    @Test
    public void onePerKm() {
        // petrol 154 g/km, EV 107.4 g/km
        assertEquals(154, calc.petrolCarGrams(1_000));
        assertEquals(107, calc.evGrams(1_000));
        assertEquals(47, calc.savedGrams(1_000));
    }

    @Test
    public void zeroOrNegativeDistance_savesNothing() {
        assertEquals(0, calc.savedGrams(0));
        assertEquals(0, calc.savedGrams(-500));
    }

    @Test
    public void dirtierEv_neverClaimsNegativeSaving() {
        Co2Calculator coalHeavy = new Co2Calculator(new EmissionFactors(250, 1_000, 2_310, 15_000));
        assertEquals(0, coalHeavy.savedGrams(10_000));
    }

    @Test
    public void kgFormatting() {
        assertEquals("0.52 kg", Co2Calculator.kg(524));
        assertEquals("0.00 kg", Co2Calculator.kg(0));
        assertEquals("12.3 kg", Co2Calculator.kg(12_340));
        assertEquals("1,234 kg", Co2Calculator.kg(1_234_000));
        assertEquals("0.524", Co2Calculator.kgPlain(524));
        assertEquals("12.005", Co2Calculator.kgPlain(12_005));
    }
}
