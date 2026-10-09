package com.efast.passenger.data;

import android.os.Handler;
import android.os.Looper;

import com.efast.passenger.data.model.ChargingBay;
import com.efast.passenger.data.model.Driver;
import com.efast.passenger.data.model.FareQuote;
import com.efast.passenger.data.model.Place;
import com.efast.passenger.data.model.RideOffer;
import com.efast.passenger.data.model.VehicleStatus;
import com.efast.passenger.util.FareCalculator;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * DEMO ONLY. Returns hardcoded data after a short delay so the screens show
 * real loading states, exactly as they will with a live backend.
 */
public class FakeRideRepository implements RideRepository {

    public static final String DEMO_OTP = "1234";

    private static final long NETWORK_DELAY_MS = 800;
    private static final long DRIVER_SEARCH_MS = 3000;

    /**
     * Straight-line distance x 1.4 approximates road distance inside Pune.
     * Demo approximation only; production distance comes from a routing API.
     */
    private static final double ROAD_FACTOR = 1.4;

    private final Handler main = new Handler(Looper.getMainLooper());

    // Approximate coordinates, good enough for demo distances.
    private final List<Place> places = Arrays.asList(
            new Place("viman_nagar", "Viman Nagar", "Near Phoenix Marketcity", 18.5679, 73.9143),
            new Place("pune_airport", "Pune Airport", "Lohegaon", 18.5821, 73.9197),
            new Place("kalyani_nagar", "Kalyani Nagar", "Central Avenue", 18.5463, 73.9033),
            new Place("koregaon_park", "Koregaon Park", "North Main Road", 18.5362, 73.8940),
            new Place("magarpatta", "Magarpatta City", "Hadapsar", 18.5158, 73.9272),
            new Place("shivajinagar", "Shivajinagar", "FC Road side", 18.5308, 73.8475),
            new Place("hinjewadi", "Hinjewadi Phase 1", "Rajiv Gandhi Infotech Park", 18.5913, 73.7389)
    );

    // ── Driver side: Home Ride demo data ──────────────────────────

    private final List<Place> localities = Arrays.asList(
            new Place("viman_nagar", "Viman Nagar", "East Pune", 18.5679, 73.9143),
            new Place("kharadi", "Kharadi", "East Pune", 18.5515, 73.9424),
            new Place("kalyani_nagar", "Kalyani Nagar", "East Pune", 18.5463, 73.9033),
            new Place("koregaon_park", "Koregaon Park", "Central Pune", 18.5362, 73.8940),
            new Place("hadapsar", "Hadapsar", "South-east Pune", 18.5089, 73.9260),
            new Place("shivajinagar", "Shivajinagar", "Central Pune", 18.5308, 73.8475),
            new Place("kothrud", "Kothrud", "West Pune", 18.5074, 73.8077),
            new Place("aundh", "Aundh", "North-west Pune", 18.5580, 73.8075),
            new Place("baner", "Baner", "North-west Pune", 18.5590, 73.7868),
            new Place("wakad", "Wakad", "North-west Pune", 18.5975, 73.7700),
            new Place("pimpri", "Pimpri", "Pimpri-Chinchwad", 18.6298, 73.7997)
    );

    /** Demo: the driver's shift ends at Hinjewadi, far from an east-Pune home. */
    private final Place driverLocation =
            new Place("hinjewadi", "Hinjewadi Phase 1", "Rajiv Gandhi Infotech Park", 18.5913, 73.7389);

    private final List<ChargingBay> chargingBays = Arrays.asList(
            new ChargingBay("hub_baner",
                    new Place("hub_baner", "EFast Hub Baner", "Baner Road", 18.5642, 73.7769), 2),
            new ChargingBay("hub_wakad",
                    new Place("hub_wakad", "EFast Hub Wakad", "Wakad Chowk", 18.5990, 73.7620), 0),
            new ChargingBay("hub_shivajinagar",
                    new Place("hub_shivajinagar", "EFast Hub Shivajinagar", "FC Road", 18.5300, 73.8530), 1),
            new ChargingBay("hub_viman_nagar",
                    new Place("hub_viman_nagar", "EFast Hub Viman Nagar", "Nagar Road", 18.5650, 73.9100), 3)
    );

    /** The four hand-picked spots for launch. */
    private final List<Place> busyZones = Arrays.asList(
            new Place("pune_airport", "Pune Airport", "Arrivals pickup, Lohegaon", 18.5821, 73.9197),
            new Place("pune_station", "Pune Station", "Main entrance, Agarkar Nagar", 18.5289, 73.8744),
            new Place("hinjewadi", "Hinjewadi Phase 1", "Rajiv Gandhi Infotech Park", 18.5913, 73.7389),
            new Place("kharadi_eon", "Kharadi", "EON IT Park", 18.5530, 73.9500)
    );

    @Override
    public void requestOtp(String phone, Callback<Void> callback) {
        main.postDelayed(() -> callback.onSuccess(null), NETWORK_DELAY_MS);
    }

    @Override
    public void verifyOtp(String phone, String otp, Callback<Boolean> callback) {
        main.postDelayed(() -> {
            if (DEMO_OTP.equals(otp)) {
                callback.onSuccess(true);
            } else {
                callback.onError("Incorrect code. Please try again.");
            }
        }, NETWORK_DELAY_MS);
    }

    @Override
    public List<Place> getPopularPlaces() {
        return Collections.unmodifiableList(places);
    }

    @Override
    public Place getPlaceById(String id) {
        for (Place p : places) {
            if (p.id.equals(id)) return p;
        }
        return null;
    }

    @Override
    public void getQuote(Place pickup, Place drop, Callback<FareQuote> callback) {
        final long meters = roadDistanceMeters(pickup, drop);
        main.postDelayed(() -> callback.onSuccess(FareCalculator.quote(meters)), NETWORK_DELAY_MS);
    }

    @Override
    public void findDriver(Place pickup, Callback<Driver> callback) {
        main.postDelayed(() -> callback.onSuccess(new Driver(
                "Rahul Jadhav", "Tata Punch EV", "MH 12 AB 4521", 4.9, 4, "4827")), DRIVER_SEARCH_MS);
    }

    @Override
    public List<Place> getLocalities() {
        return Collections.unmodifiableList(localities);
    }

    @Override
    public Place getDriverLocation() {
        return driverLocation;
    }

    @Override
    public VehicleStatus getVehicleStatus() {
        return new VehicleStatus(64, 165_000);
    }

    @Override
    public List<ChargingBay> getChargingBays() {
        return Collections.unmodifiableList(chargingBays);
    }

    @Override
    public List<Place> getBusyZones() {
        return Collections.unmodifiableList(busyZones);
    }

    /**
     * Scripted so a demo shows every rule: the first three are filtered out
     * (wrong direction, detour too long, rider would wait longer), then two that go
     * toward an east-Pune home, then one more in the wrong direction.
     */
    @Override
    public void getNearbyRideRequests(Place from, Callback<List<RideOffer>> callback) {
        Place hinjewadi2 = new Place("hinjewadi_2", "Hinjewadi Phase 2", "Infosys Circle", 18.5880, 73.7020);
        Place talegaon = new Place("talegaon", "Talegaon", "Talegaon Dabhade", 18.7350, 73.6750);
        Place wakad = place("wakad");
        Place kothrud = place("kothrud");
        Place shivajinagar = place("shivajinagar");
        Place baner = place("baner");
        Place koregaonPark = place("koregaon_park");
        Place aundh = place("aundh");
        Place vimanNagar = place("viman_nagar");
        Place pimpri = place("pimpri");
        List<RideOffer> offers = Arrays.asList(
                new RideOffer("r1", hinjewadi2, talegaon, 5, 6),
                new RideOffer("r2", wakad, kothrud, 6, 8),
                new RideOffer("r3", wakad, shivajinagar, 9, 3),
                new RideOffer("r4", baner, koregaonPark, 4, 6),
                new RideOffer("r5", aundh, vimanNagar, 7, RideOffer.NO_OTHER_DRIVER),
                new RideOffer("r6", driverLocation, pimpri, 2, 5)
        );
        main.postDelayed(() -> callback.onSuccess(offers), NETWORK_DELAY_MS);
    }

    private Place place(String localityId) {
        for (Place p : localities) {
            if (p.id.equals(localityId)) return p;
        }
        throw new IllegalArgumentException(localityId);
    }

    /** Haversine straight-line distance, multiplied by ROAD_FACTOR. */
    static long roadDistanceMeters(Place a, Place b) {
        final double earthRadiusM = 6_371_000d;
        double dLat = Math.toRadians(b.lat - a.lat);
        double dLng = Math.toRadians(b.lng - a.lng);
        double h = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(a.lat)) * Math.cos(Math.toRadians(b.lat))
                * Math.sin(dLng / 2) * Math.sin(dLng / 2);
        double straightLine = 2 * earthRadiusM * Math.asin(Math.sqrt(h));
        return Math.round(straightLine * ROAD_FACTOR);
    }
}
