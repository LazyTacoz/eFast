package com.efast.passenger.green;

import com.efast.passenger.data.model.GreenTrip;

import org.junit.Test;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ReportCsvTest {

    @Test
    public void csvHasHeaderRowsAndTotal() {
        Co2Calculator calc = new Co2Calculator(EmissionFactors.indiaDefaults());
        EmissionsReport r = EmissionsReport.build("Acme, Pune", YearMonth.of(2026, 10), Arrays.asList(
                new GreenTrip("Amit", LocalDate.of(2026, 10, 2), 11_250)), calc);
        String csv = ReportCsv.toCsv(r);
        String[] lines = csv.split("\r\n");

        assertEquals("Account,\"Acme, Pune\"", lines[0]);
        assertEquals("Month,2026-10", lines[1]);
        assertTrue(lines[2].startsWith("Method,EFast EV at 150 Wh/km x 716 g CO2/kWh"));
        assertTrue(lines[2].endsWith("2310 g CO2/L and 15 km/L"));
        assertEquals("", lines[3]);
        assertEquals("Rider,Rides,Distance (km),Petrol car CO2 (kg),EFast CO2 (kg),CO2 saved (kg)", lines[4]);
        assertEquals("Amit,1,11.250,1.733,1.208,0.524", lines[5]);
        assertEquals("Total,1,11.250,1.733,1.208,0.524", lines[6]);
    }

    @Test
    public void escapesQuotesCommasAndFormulas() {
        assertEquals("plain", ReportCsv.escape("plain"));
        assertEquals("\"a,b\"", ReportCsv.escape("a,b"));
        assertEquals("\"say \"\"hi\"\"\"", ReportCsv.escape("say \"hi\""));
        assertEquals("'=1+1", ReportCsv.escape("=1+1"));
        assertEquals("-5", ReportCsv.escape("-5"));
    }
}
