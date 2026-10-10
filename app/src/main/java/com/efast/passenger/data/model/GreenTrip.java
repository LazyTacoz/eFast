package com.efast.passenger.data.model;

import java.time.LocalDate;

/** One completed EFast ride, as needed for emissions reports. */
public final class GreenTrip {
    public final String riderName;
    public final LocalDate date;
    public final long distanceMeters;

    public GreenTrip(String riderName, LocalDate date, long distanceMeters) {
        this.riderName = riderName;
        this.date = date;
        this.distanceMeters = distanceMeters;
    }
}
