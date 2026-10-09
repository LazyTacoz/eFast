package com.efast.passenger.homeride;

import com.efast.passenger.data.model.Place;

/**
 * Distance maths for Home Ride. Same approximation as FakeRideRepository:
 * straight-line (haversine) distance x 1.4 to stand in for road distance in Pune.
 * Production should use road distances from a routing API instead.
 */
public final class Geo {

    static final double ROAD_FACTOR = 1.4;
    private static final double EARTH_RADIUS_M = 6_371_000d;

    private Geo() {
    }

    /** Approximate road distance between two places, in metres. */
    public static long roadMeters(Place a, Place b) {
        double dLat = Math.toRadians(b.lat - a.lat);
        double dLng = Math.toRadians(b.lng - a.lng);
        double h = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(a.lat)) * Math.cos(Math.toRadians(b.lat))
                * Math.sin(dLng / 2) * Math.sin(dLng / 2);
        double straightLine = 2 * EARTH_RADIUS_M * Math.asin(Math.sqrt(h));
        return Math.round(straightLine * ROAD_FACTOR);
    }
}
