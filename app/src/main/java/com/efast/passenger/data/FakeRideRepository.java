package com.efast.passenger.data;

import android.os.Handler;
import android.os.Looper;

import com.efast.passenger.data.model.Driver;
import com.efast.passenger.data.model.FareQuote;
import com.efast.passenger.data.model.Place;
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
