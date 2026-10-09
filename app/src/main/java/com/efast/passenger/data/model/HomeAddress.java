package com.efast.passenger.data.model;

/**
 * A driver's saved home. Sensitive personal data (DPDP Act): stored encrypted on the
 * device by HomeAddressStore, never sent to riders, and shown elsewhere only as
 * {@link #areaName} (the general area).
 */
public final class HomeAddress {
    /** Flat / street line, as the driver typed it. Only the driver sees this. */
    public final String addressLine;
    /** General area, e.g. "Kharadi". Safe to show in summaries. */
    public final String areaName;
    public final double lat;
    public final double lng;

    public HomeAddress(String addressLine, String areaName, double lat, double lng) {
        this.addressLine = addressLine;
        this.areaName = areaName;
        this.lat = lat;
        this.lng = lng;
    }

    public Place asPlace() {
        return new Place("home", areaName, areaName, lat, lng);
    }

    /** Never prints the address line or coordinates, so logs can't leak them. */
    @Override
    public String toString() {
        return "HomeAddress{area=" + areaName + "}";
    }
}
