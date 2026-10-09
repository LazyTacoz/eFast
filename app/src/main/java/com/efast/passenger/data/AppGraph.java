package com.efast.passenger.data;

import android.content.Context;

/**
 * The ONE place that decides which repository the app uses.
 * After the demo, change `new FakeRideRepository()` to the real API implementation.
 */
public final class AppGraph {

    private static RideRepository rideRepository;
    private static HomeAddressStore homeAddressStore;
    private static HomeRideUsageStore homeRideUsageStore;

    private AppGraph() {
    }

    public static synchronized RideRepository rideRepository() {
        if (rideRepository == null) {
            rideRepository = new FakeRideRepository();
        }
        return rideRepository;
    }

    public static synchronized HomeAddressStore homeAddressStore(Context context) {
        if (homeAddressStore == null) {
            homeAddressStore = new HomeAddressStore(context);
        }
        return homeAddressStore;
    }

    public static synchronized HomeRideUsageStore homeRideUsageStore(Context context) {
        if (homeRideUsageStore == null) {
            homeRideUsageStore = new HomeRideUsageStore(context);
        }
        return homeRideUsageStore;
    }
}
