package com.efast.passenger.data;

import android.content.Context;
import android.content.SharedPreferences;

import com.efast.passenger.data.model.GreenTrip;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * The rider's own completed rides (date + distance), so the app can show a running
 * CO2 total and a monthly report. Demo: kept on the phone only; production reads the
 * rider's trip history from the server.
 */
public final class GreenRideStore {

    public static final String RIDER_SELF = "You";

    private static final String PREFS = "green_rides";
    private static final String KEY_RIDES = "rides";
    /** Enough for a year of daily rides; older ones drop off. */
    private static final int MAX_RIDES = 400;

    private final SharedPreferences prefs;

    GreenRideStore(Context context) {
        prefs = context.getApplicationContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    /** Oldest first. */
    public List<GreenTrip> rides() {
        List<GreenTrip> rides = new ArrayList<>();
        String raw = prefs.getString(KEY_RIDES, "");
        for (String part : raw.split(",")) {
            int sep = part.indexOf(':');
            if (sep < 0) continue;
            try {
                rides.add(new GreenTrip(RIDER_SELF, LocalDate.parse(part.substring(0, sep)),
                        Long.parseLong(part.substring(sep + 1))));
            } catch (RuntimeException ignored) {
                // Skip a damaged entry rather than lose the rest.
            }
        }
        return rides;
    }

    public void recordRide(LocalDate day, long distanceMeters) {
        List<GreenTrip> rides = rides();
        rides.add(new GreenTrip(RIDER_SELF, day, distanceMeters));
        int from = Math.max(0, rides.size() - MAX_RIDES);
        StringBuilder sb = new StringBuilder();
        for (int i = from; i < rides.size(); i++) {
            if (sb.length() > 0) sb.append(',');
            sb.append(rides.get(i).date).append(':').append(rides.get(i).distanceMeters);
        }
        prefs.edit().putString(KEY_RIDES, sb.toString()).apply();
    }
}
