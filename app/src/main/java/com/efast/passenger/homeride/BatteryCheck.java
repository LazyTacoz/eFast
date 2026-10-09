package com.efast.passenger.homeride;

import com.efast.passenger.data.model.ChargingBay;
import com.efast.passenger.data.model.Place;

import java.util.List;

/**
 * Before every offer: can the car do this trip and still get home with a safety buffer?
 * If not, pick a free charging bay to stop at first, preferring the one that adds the
 * least distance on the way home.
 */
public final class BatteryCheck {

    private final HomeRideRules rules;

    public BatteryCheck(HomeRideRules rules) {
        this.rules = rules;
    }

    /** Range the car needs for a trip of this length, including buffer and reserve. */
    public long requiredRangeMeters(long tripMeters) {
        return Math.round(tripMeters * (1 + rules.batteryBufferRatio)) + rules.batteryReserveMeters;
    }

    public boolean canMakeIt(long rangeMeters, long tripMeters) {
        return rangeMeters >= requiredRangeMeters(tripMeters);
    }

    /**
     * Best free bay to charge at before continuing home: reachable on the current range
     * (with buffer), and adding the least extra distance versus driving straight home.
     * Returns null if no free bay is reachable.
     */
    public ChargingBay pickChargingBay(long rangeMeters, Place driver, Place home, List<ChargingBay> bays) {
        long directHome = Geo.roadMeters(driver, home);
        ChargingBay best = null;
        long bestExtra = Long.MAX_VALUE;
        for (ChargingBay bay : bays) {
            if (bay.freeSlots <= 0) continue;
            long toBay = Geo.roadMeters(driver, bay.location);
            if (!canMakeIt(rangeMeters, toBay)) continue;
            long extra = toBay + Geo.roadMeters(bay.location, home) - directHome;
            if (extra < bestExtra) {
                bestExtra = extra;
                best = bay;
            }
        }
        return best;
    }
}
