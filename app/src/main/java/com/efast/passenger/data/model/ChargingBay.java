package com.efast.passenger.data.model;

/** An EFast charging bay. Demo data is in FakeRideRepository. */
public final class ChargingBay {
    public final String id;
    public final Place location;
    public final int freeSlots;

    public ChargingBay(String id, Place location, int freeSlots) {
        this.id = id;
        this.location = location;
        this.freeSlots = freeSlots;
    }
}
