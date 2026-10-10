package com.efast.passenger.green;

import java.util.Locale;

/**
 * The numbers behind every CO2 figure in the app. Change them here only.
 *
 * Comparison: an EFast EV ride vs the same distance in a typical petrol car.
 *   EV     = energy drawn from the grid x India's grid emission factor
 *            (includes charging losses, so it counts power-station emissions)
 *   Petrol = CO2 from burning the petrol (tailpipe only; refining and transport of
 *            the fuel are left out, which keeps the saving on the conservative side)
 *
 * Sources for the defaults (re-check them once a year):
 *   - India grid: CEA "CO2 Baseline Database for the Indian Power Sector", v19
 *     (FY 2022-23), weighted average 0.716 t CO2/MWh = 716 g/kWh.
 *   - EV energy use: 150 Wh/km from the plug, typical for a Tata Punch EV / Nexon EV
 *     in city traffic including charging losses. Replace with EFast fleet data.
 *   - Petrol: 2,310 g CO2 per litre burned (IPCC default for motor gasoline),
 *     and 15 km/litre for a typical small petrol car in Pune traffic.
 */
public final class EmissionFactors {

    public final long evWattHoursPerKm;
    public final long gridGramsCo2PerKwh;
    public final long petrolGramsCo2PerLitre;
    public final long petrolCarMetersPerLitre;

    public EmissionFactors(long evWattHoursPerKm, long gridGramsCo2PerKwh,
                           long petrolGramsCo2PerLitre, long petrolCarMetersPerLitre) {
        this.evWattHoursPerKm = evWattHoursPerKm;
        this.gridGramsCo2PerKwh = gridGramsCo2PerKwh;
        this.petrolGramsCo2PerLitre = petrolGramsCo2PerLitre;
        this.petrolCarMetersPerLitre = petrolCarMetersPerLitre;
    }

    /** 15_000 -> "15", 15_500 -> "15.5" */
    public String petrolKmPerLitreText() {
        String text = String.format(Locale.ENGLISH, "%.2f", petrolCarMetersPerLitre / 1000.0);
        return text.replaceAll("\\.?0+$", "");
    }

    public static EmissionFactors indiaDefaults() {
        return new EmissionFactors(150, 716, 2_310, 15_000);
    }
}
