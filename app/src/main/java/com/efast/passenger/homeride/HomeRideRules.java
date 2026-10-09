package com.efast.passenger.homeride;

/**
 * Every Home Ride limit in one place, so ops can tune them later without touching the logic.
 * Use {@link #defaults()} in the app; tests build their own values.
 */
public final class HomeRideRules {

    /** How many times a driver can turn Home Ride on per calendar day. */
    public final int maxUsesPerDay;
    /** How many times a driver can change their saved home per calendar month (first save is free). */
    public final int maxAddressChangesPerMonth;

    /** A ride that drops within this distance of home always counts as "toward home". */
    public final long nearHomeMeters;
    /** Otherwise the ride must cut the distance to home by at least this much... */
    public final long minProgressMeters;
    /** ...and by at least this share of the current distance to home (0.25 = 25%). */
    public final double minProgressRatio;

    /** Extra driving allowed versus going straight home: the larger of these two. */
    public final long minDetourAllowanceMeters;
    public final double maxDetourRatio;

    /** Battery: needed range = trip x (1 + buffer) + reserve. */
    public final double batteryBufferRatio;
    public final long batteryReserveMeters;

    /** Time window choices offered to the driver, in minutes. */
    public final int[] windowOptionsMinutes;
    /** Suggest a busy zone on the way home if nothing matched after this long. */
    public final int busyZoneAfterMinutes;

    public HomeRideRules(int maxUsesPerDay, int maxAddressChangesPerMonth,
                         long nearHomeMeters, long minProgressMeters, double minProgressRatio,
                         long minDetourAllowanceMeters, double maxDetourRatio,
                         double batteryBufferRatio, long batteryReserveMeters,
                         int[] windowOptionsMinutes, int busyZoneAfterMinutes) {
        this.maxUsesPerDay = maxUsesPerDay;
        this.maxAddressChangesPerMonth = maxAddressChangesPerMonth;
        this.nearHomeMeters = nearHomeMeters;
        this.minProgressMeters = minProgressMeters;
        this.minProgressRatio = minProgressRatio;
        this.minDetourAllowanceMeters = minDetourAllowanceMeters;
        this.maxDetourRatio = maxDetourRatio;
        this.batteryBufferRatio = batteryBufferRatio;
        this.batteryReserveMeters = batteryReserveMeters;
        this.windowOptionsMinutes = windowOptionsMinutes;
        this.busyZoneAfterMinutes = busyZoneAfterMinutes;
    }

    /** Launch defaults. */
    public static HomeRideRules defaults() {
        return new HomeRideRules(
                2,          // uses per day
                2,          // home address changes per month
                3_000,      // "near home" radius
                2_000,      // min progress toward home
                0.25,       // min progress as a share of the remaining distance
                3_000,      // detour allowance floor
                0.30,       // detour allowance as a share of the direct trip home
                0.20,       // battery safety buffer (20%)
                5_000,      // battery reserve on arrival (5 km)
                new int[]{60, 75, 90},
                15);
    }
}
