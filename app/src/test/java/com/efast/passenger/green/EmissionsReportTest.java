package com.efast.passenger.green;

import com.efast.passenger.data.model.GreenTrip;

import org.junit.Test;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class EmissionsReportTest {

    private final Co2Calculator calc = new Co2Calculator(EmissionFactors.indiaDefaults());
    private final YearMonth october = YearMonth.of(2026, 10);

    private final List<GreenTrip> trips = Arrays.asList(
            new GreenTrip("Priya", LocalDate.of(2026, 10, 1), 20_000),
            new GreenTrip("Priya", LocalDate.of(2026, 10, 2), 20_000),
            new GreenTrip("Amit", LocalDate.of(2026, 10, 2), 11_250),
            new GreenTrip("Amit", LocalDate.of(2026, 9, 30), 50_000),   // September: left out
            new GreenTrip("Amit", LocalDate.of(2026, 11, 1), 50_000)    // November: left out
    );

    @Test
    public void onlyCountsTheChosenMonth() {
        EmissionsReport r = EmissionsReport.build("Acme", october, trips, calc);
        assertEquals(3, r.rides);
        assertEquals(51_250, r.distanceMeters);
    }

    @Test
    public void rowsAreSortedByCo2Saved_andAddUpToTheTotal() {
        EmissionsReport r = EmissionsReport.build("Acme", october, trips, calc);
        assertEquals(2, r.rows.size());
        assertEquals("Priya", r.rows.get(0).riderName);
        assertEquals(2, r.rows.get(0).rides);
        assertEquals(2 * calc.savedGrams(20_000), r.rows.get(0).savedGrams);
        assertEquals("Amit", r.rows.get(1).riderName);
        assertEquals(524, r.rows.get(1).savedGrams);

        long sum = 0;
        for (EmissionsReport.Row row : r.rows) sum += row.savedGrams;
        assertEquals(sum, r.savedGrams);
        assertEquals(r.petrolCarGrams - r.evGrams, r.savedGrams, 3); // per-ride rounding only
    }

    @Test
    public void emptyMonth_isAllZeros() {
        EmissionsReport r = EmissionsReport.build("Acme", october, Collections.emptyList(), calc);
        assertTrue(r.rows.isEmpty());
        assertEquals(0, r.rides);
        assertEquals(0, r.savedGrams);
    }
}
