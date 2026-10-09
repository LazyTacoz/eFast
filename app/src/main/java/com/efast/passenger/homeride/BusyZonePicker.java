package com.efast.passenger.homeride;

import com.efast.passenger.data.model.Place;

import java.util.List;

/**
 * Launch version of busy-zone suggestions: picks from a hand-picked list of known busy
 * spots (airport, station, IT parks) the one that adds the least distance on the way home.
 * Phase 2 replaces the hand-picked list with zones learned from trip data.
 */
public final class BusyZonePicker {

    private final HomeRideRules rules;

    public BusyZonePicker(HomeRideRules rules) {
        this.rules = rules;
    }

    /**
     * Only zones that are closer to home than the driver is now, and not already next to
     * home (waiting there is just going home). Returns null if none qualifies.
     */
    public Place pick(Place driver, Place home, List<Place> zones) {
        long directHome = Geo.roadMeters(driver, home);
        Place best = null;
        long bestExtra = Long.MAX_VALUE;
        for (Place zone : zones) {
            long zoneToHome = Geo.roadMeters(zone, home);
            if (zoneToHome >= directHome || zoneToHome <= rules.nearHomeMeters) continue;
            long extra = Geo.roadMeters(driver, zone) + zoneToHome - directHome;
            if (extra < bestExtra) {
                bestExtra = extra;
                best = zone;
            }
        }
        return best;
    }
}
