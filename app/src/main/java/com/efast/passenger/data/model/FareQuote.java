package com.efast.passenger.data.model;

/**
 * A fare broken into line items. All money is held in PAISE (long), never float/double,
 * so GST and totals never pick up rounding errors.
 */
public final class FareQuote {
    public final long distanceMeters;
    public final long baseFarePaise;
    public final long gstPaise;
    public final long totalPaise;

    public FareQuote(long distanceMeters, long baseFarePaise, long gstPaise, long totalPaise) {
        this.distanceMeters = distanceMeters;
        this.baseFarePaise = baseFarePaise;
        this.gstPaise = gstPaise;
        this.totalPaise = totalPaise;
    }
}
