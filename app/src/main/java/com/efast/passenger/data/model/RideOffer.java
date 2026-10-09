package com.efast.passenger.data.model;

/** A ride request that could be offered to a driver. Demo data is in FakeRideRepository. */
public final class RideOffer {

    /** bestOtherDriverEtaMinutes value when no regular driver is available for this rider. */
    public static final int NO_OTHER_DRIVER = -1;

    public final String id;
    public final Place pickup;
    public final Place drop;
    /** How long this driver would take to reach the pickup. */
    public final int etaToPickupMinutes;
    /** ETA of the best regular (non Home Ride) driver for this rider, or NO_OTHER_DRIVER. */
    public final int bestOtherDriverEtaMinutes;

    public RideOffer(String id, Place pickup, Place drop, int etaToPickupMinutes, int bestOtherDriverEtaMinutes) {
        this.id = id;
        this.pickup = pickup;
        this.drop = drop;
        this.etaToPickupMinutes = etaToPickupMinutes;
        this.bestOtherDriverEtaMinutes = bestOtherDriverEtaMinutes;
    }
}
