package com.efast.passenger.homeride;

/**
 * The time window the driver picked when turning Home Ride on (e.g. 60 to 90 minutes).
 * When it runs out with no match, the driver simply goes home: no penalty.
 */
public final class HomeRideWindow {

    public final int windowMinutes;
    private final int busyZoneAfterMinutes;

    public HomeRideWindow(int windowMinutes, HomeRideRules rules) {
        this.windowMinutes = windowMinutes;
        this.busyZoneAfterMinutes = rules.busyZoneAfterMinutes;
    }

    public int minutesLeft(int elapsedMinutes) {
        return Math.max(0, windowMinutes - elapsedMinutes);
    }

    public boolean isExpired(int elapsedMinutes) {
        return elapsedMinutes >= windowMinutes;
    }

    public boolean shouldSuggestBusyZone(int elapsedMinutes, boolean matchedYet) {
        return !matchedYet && !isExpired(elapsedMinutes) && elapsedMinutes >= busyZoneAfterMinutes;
    }
}
