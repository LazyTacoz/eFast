package com.efast.passenger.data.model;

/** A pickup or drop location. Demo data is hardcoded in FakeRideRepository. */
public final class Place {
    public final String id;
    public final String name;
    public final String area;
    public final double lat;
    public final double lng;

    public Place(String id, String name, String area, double lat, double lng) {
        this.id = id;
        this.name = name;
        this.area = area;
        this.lat = lat;
        this.lng = lng;
    }
}
