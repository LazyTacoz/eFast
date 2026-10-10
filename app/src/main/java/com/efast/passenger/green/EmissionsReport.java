package com.efast.passenger.green;

import com.efast.passenger.data.model.GreenTrip;

import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * One account's emissions saved in one calendar month, with a line per rider.
 * Totals are the sum of the per-ride figures, so the rows always add up to the total.
 */
public final class EmissionsReport {

    public static final class Row {
        public final String riderName;
        public final int rides;
        public final long distanceMeters;
        public final long petrolCarGrams;
        public final long evGrams;
        public final long savedGrams;

        Row(String riderName, int rides, long distanceMeters,
            long petrolCarGrams, long evGrams, long savedGrams) {
            this.riderName = riderName;
            this.rides = rides;
            this.distanceMeters = distanceMeters;
            this.petrolCarGrams = petrolCarGrams;
            this.evGrams = evGrams;
            this.savedGrams = savedGrams;
        }
    }

    public final String accountName;
    public final YearMonth month;
    public final EmissionFactors factors;
    /** Most CO2 saved first. */
    public final List<Row> rows;
    public final int rides;
    public final long distanceMeters;
    public final long petrolCarGrams;
    public final long evGrams;
    public final long savedGrams;

    private EmissionsReport(String accountName, YearMonth month, EmissionFactors factors, List<Row> rows) {
        this.accountName = accountName;
        this.month = month;
        this.factors = factors;
        this.rows = Collections.unmodifiableList(rows);
        int r = 0;
        long d = 0, p = 0, e = 0, s = 0;
        for (Row row : rows) {
            r += row.rides;
            d += row.distanceMeters;
            p += row.petrolCarGrams;
            e += row.evGrams;
            s += row.savedGrams;
        }
        rides = r;
        distanceMeters = d;
        petrolCarGrams = p;
        evGrams = e;
        savedGrams = s;
    }

    /** Trips outside {@code month} are ignored, so callers can pass a rider's whole history. */
    public static EmissionsReport build(String accountName, YearMonth month,
                                        List<GreenTrip> trips, Co2Calculator calc) {
        Map<String, long[]> byRider = new LinkedHashMap<>();
        for (GreenTrip t : trips) {
            if (!YearMonth.from(t.date).equals(month)) continue;
            long[] sums = byRider.get(t.riderName);
            if (sums == null) {
                sums = new long[5];
                byRider.put(t.riderName, sums);
            }
            sums[0]++;
            sums[1] += t.distanceMeters;
            sums[2] += calc.petrolCarGrams(t.distanceMeters);
            sums[3] += calc.evGrams(t.distanceMeters);
            sums[4] += calc.savedGrams(t.distanceMeters);
        }
        List<Row> rows = new ArrayList<>();
        for (Map.Entry<String, long[]> e : byRider.entrySet()) {
            long[] s = e.getValue();
            rows.add(new Row(e.getKey(), (int) s[0], s[1], s[2], s[3], s[4]));
        }
        rows.sort((a, b) -> {
            int bySaved = Long.compare(b.savedGrams, a.savedGrams);
            return bySaved != 0 ? bySaved : a.riderName.compareTo(b.riderName);
        });
        return new EmissionsReport(accountName, month, calc.factors(), rows);
    }
}
