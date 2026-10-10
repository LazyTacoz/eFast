package com.efast.passenger.green;

import java.util.Locale;

/** Turns an EmissionsReport into a CSV that opens cleanly in Excel or Google Sheets. */
public final class ReportCsv {

    private ReportCsv() {
    }

    public static String toCsv(EmissionsReport report) {
        EmissionFactors f = report.factors;
        StringBuilder sb = new StringBuilder();
        line(sb, "Account", report.accountName);
        line(sb, "Month", report.month.toString());
        line(sb, "Method", String.format(Locale.ENGLISH,
                "EFast EV at %d Wh/km x %d g CO2/kWh (India grid) vs petrol car at %d g CO2/L and %s km/L",
                f.evWattHoursPerKm, f.gridGramsCo2PerKwh, f.petrolGramsCo2PerLitre,
                f.petrolKmPerLitreText()));
        sb.append("\r\n");
        line(sb, "Rider", "Rides", "Distance (km)", "Petrol car CO2 (kg)", "EFast CO2 (kg)", "CO2 saved (kg)");
        for (EmissionsReport.Row r : report.rows) {
            line(sb, r.riderName, String.valueOf(r.rides), decimal(r.distanceMeters),
                    Co2Calculator.kgPlain(r.petrolCarGrams), Co2Calculator.kgPlain(r.evGrams),
                    Co2Calculator.kgPlain(r.savedGrams));
        }
        line(sb, "Total", String.valueOf(report.rides), decimal(report.distanceMeters),
                Co2Calculator.kgPlain(report.petrolCarGrams), Co2Calculator.kgPlain(report.evGrams),
                Co2Calculator.kgPlain(report.savedGrams));
        return sb.toString();
    }

    /** 11250 -> "11.250" (thousandths, so metres -> km and grams -> kg) */
    private static String decimal(long thousandths) {
        return String.format(Locale.ENGLISH, "%d.%03d", thousandths / 1000, thousandths % 1000);
    }

    private static void line(StringBuilder sb, String... fields) {
        for (int i = 0; i < fields.length; i++) {
            if (i > 0) sb.append(',');
            sb.append(escape(fields[i]));
        }
        sb.append("\r\n");
    }

    static String escape(String field) {
        String safe = field;
        // Stop a spreadsheet from running a rider name like "=HYPERLINK(...)" as a formula.
        if (!safe.isEmpty() && "=+-@".indexOf(safe.charAt(0)) >= 0 && !looksNumeric(safe)) {
            safe = "'" + safe;
        }
        if (safe.contains(",") || safe.contains("\"") || safe.contains("\n") || safe.contains("\r")) {
            return '"' + safe.replace("\"", "\"\"") + '"';
        }
        return safe;
    }

    private static boolean looksNumeric(String s) {
        return s.matches("-?\\d+(\\.\\d+)?");
    }
}
