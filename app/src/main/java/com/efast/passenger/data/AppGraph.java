package com.efast.passenger.data;

/**
 * The ONE place that decides which repository the app uses.
 * After the demo, change `new FakeRideRepository()` to the real API implementation.
 */
public final class AppGraph {

    private static RideRepository rideRepository;

    private AppGraph() {
    }

    public static synchronized RideRepository rideRepository() {
        if (rideRepository == null) {
            rideRepository = new FakeRideRepository();
        }
        return rideRepository;
    }
}
