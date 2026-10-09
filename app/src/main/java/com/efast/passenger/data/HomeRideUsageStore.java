package com.efast.passenger.data;

import android.content.Context;
import android.content.SharedPreferences;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Remembers when Home Ride was turned on and when the home address was changed, so
 * HomeRideLimits can enforce the daily and monthly caps. Demo: on the phone only;
 * production must enforce these on the server, where a reinstall can't reset them.
 */
public final class HomeRideUsageStore {

    private static final String PREFS = "home_ride_usage";
    private static final String KEY_USES = "uses";
    private static final String KEY_ADDRESS_CHANGES = "address_changes";
    /** Older entries can't affect a daily or monthly limit. */
    private static final int KEEP_DAYS = 40;

    private final SharedPreferences prefs;

    HomeRideUsageStore(Context context) {
        prefs = context.getApplicationContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public List<LocalDate> uses() {
        return read(KEY_USES);
    }

    public List<LocalDate> addressChanges() {
        return read(KEY_ADDRESS_CHANGES);
    }

    public void recordUse(LocalDate day) {
        append(KEY_USES, day);
    }

    public void recordAddressChange(LocalDate day) {
        append(KEY_ADDRESS_CHANGES, day);
    }

    private List<LocalDate> read(String key) {
        List<LocalDate> dates = new ArrayList<>();
        String raw = prefs.getString(key, "");
        for (String part : raw.split(",")) {
            if (!part.isEmpty()) dates.add(LocalDate.parse(part));
        }
        return dates;
    }

    private void append(String key, LocalDate day) {
        LocalDate cutoff = day.minusDays(KEEP_DAYS);
        StringBuilder sb = new StringBuilder();
        for (LocalDate d : read(key)) {
            if (d.isBefore(cutoff)) continue;
            sb.append(d).append(',');
        }
        sb.append(day);
        prefs.edit().putString(key, sb.toString()).apply();
    }
}
