package com.efast.passenger.data;

import com.efast.passenger.data.model.ChargingBay;
import com.efast.passenger.data.model.Driver;
import com.efast.passenger.data.model.FareQuote;
import com.efast.passenger.data.model.Place;
import com.efast.passenger.data.model.RideOffer;
import com.efast.passenger.data.model.VehicleStatus;

import java.util.List;

/**
 * Everything the passenger screens need from the backend.
 * Screens only ever talk to this interface. For the demo, AppGraph hands out
 * FakeRideRepository; after the demo, a real implementation calling the
 * EFast API replaces it and no screen code changes.
 */
public interface RideRepository {
    void requestOtp(String phone, Callback<Void> callback);

    void verifyOtp(String phone, String otp, Callback<Boolean> callback);

    List<Place> getPopularPlaces();

    Place getPlaceById(String id);

    void getQuote(Place pickup, Place drop, Callback<FareQuote> callback);

    void findDriver(Place pickup, Callback<Driver> callback);

    // ── Driver side: Home Ride (demo prototype) ────────────────────

    /** Pune localities a driver picks their home area from; also used to show only the general area. */
    List<Place> getLocalities();

    /** Where the driver is right now. Demo: fixed at the end of a shift; production: GPS. */
    Place getDriverLocation();

    VehicleStatus getVehicleStatus();

    List<ChargingBay> getChargingBays();

    /** Launch version: a hand-picked list of busy spots. Phase 2 learns these from trip data. */
    List<Place> getBusyZones();

    /** Ride requests near the driver, in the order they come in. Home Ride filters them on the phone. */
    void getNearbyRideRequests(Place driverLocation, Callback<List<RideOffer>> callback);
}
