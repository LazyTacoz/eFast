package com.efast.passenger.data;

import com.efast.passenger.data.model.Driver;
import com.efast.passenger.data.model.FareQuote;
import com.efast.passenger.data.model.Place;

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
}
