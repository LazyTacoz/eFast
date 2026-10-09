package com.efast.passenger.homeride;

import com.efast.passenger.data.model.Place;

/** Approximate Pune coordinates shared by the Home Ride tests. */
final class PuneTestPlaces {

    static final Place HINJEWADI = place("hinjewadi", 18.5913, 73.7389);
    static final Place HINJEWADI_2 = place("hinjewadi_2", 18.5880, 73.7020);
    static final Place TALEGAON = place("talegaon", 18.7350, 73.6750);
    static final Place WAKAD = place("wakad", 18.5975, 73.7700);
    static final Place BANER = place("baner", 18.5590, 73.7868);
    static final Place AUNDH = place("aundh", 18.5580, 73.8075);
    static final Place KOTHRUD = place("kothrud", 18.5074, 73.8077);
    static final Place SHIVAJINAGAR = place("shivajinagar", 18.5308, 73.8475);
    static final Place KOREGAON_PARK = place("koregaon_park", 18.5362, 73.8940);
    static final Place PUNE_STATION = place("pune_station", 18.5289, 73.8744);
    static final Place PUNE_AIRPORT = place("pune_airport", 18.5821, 73.9197);
    static final Place VIMAN_NAGAR = place("viman_nagar", 18.5679, 73.9143);
    static final Place KHARADI = place("kharadi", 18.5515, 73.9424);
    static final Place KHARADI_EON = place("kharadi_eon", 18.5530, 73.9500);

    private PuneTestPlaces() {
    }

    static Place place(String id, double lat, double lng) {
        return new Place(id, id, id, lat, lng);
    }
}
