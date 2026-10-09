package com.efast.passenger.data.model;

/** The driver's car battery, as reported by the vehicle. */
public final class VehicleStatus {
    public final int batteryPercent;
    public final long rangeMeters;

    public VehicleStatus(int batteryPercent, long rangeMeters) {
        this.batteryPercent = batteryPercent;
        this.rangeMeters = rangeMeters;
    }
}
