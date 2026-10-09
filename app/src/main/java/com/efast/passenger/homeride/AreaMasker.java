package com.efast.passenger.homeride;

import com.efast.passenger.data.model.Place;

import java.util.List;

/**
 * Privacy (DPDP Act): a driver's home is sensitive personal data. Anywhere outside the
 * driver's own "set home" screen, including ops and summary views, show only the general
 * area, never the address line or exact coordinates. Riders never see either.
 */
public final class AreaMasker {

    /** Further than this from every known locality, we only say the city. */
    static final long MAX_LOCALITY_METERS = 7_000;
    static final String CITY_FALLBACK = "Pune";

    private AreaMasker() {
    }

    public static String generalArea(Place point, List<Place> localities) {
        Place nearest = null;
        long nearestMeters = Long.MAX_VALUE;
        for (Place l : localities) {
            long m = Geo.roadMeters(point, l);
            if (m < nearestMeters) {
                nearestMeters = m;
                nearest = l;
            }
        }
        if (nearest == null || nearestMeters > MAX_LOCALITY_METERS) return CITY_FALLBACK;
        return nearest.name;
    }
}
