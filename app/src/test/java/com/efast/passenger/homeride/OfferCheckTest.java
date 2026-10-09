package com.efast.passenger.homeride;

import com.efast.passenger.data.model.Place;
import com.efast.passenger.data.model.RideOffer;

import org.junit.Test;

import static com.efast.passenger.homeride.PuneTestPlaces.AUNDH;
import static com.efast.passenger.homeride.PuneTestPlaces.BANER;
import static com.efast.passenger.homeride.PuneTestPlaces.HINJEWADI;
import static com.efast.passenger.homeride.PuneTestPlaces.HINJEWADI_2;
import static com.efast.passenger.homeride.PuneTestPlaces.KHARADI;
import static com.efast.passenger.homeride.PuneTestPlaces.KOREGAON_PARK;
import static com.efast.passenger.homeride.PuneTestPlaces.KOTHRUD;
import static com.efast.passenger.homeride.PuneTestPlaces.SHIVAJINAGAR;
import static com.efast.passenger.homeride.PuneTestPlaces.TALEGAON;
import static com.efast.passenger.homeride.PuneTestPlaces.VIMAN_NAGAR;
import static com.efast.passenger.homeride.PuneTestPlaces.WAKAD;
import static com.efast.passenger.homeride.PuneTestPlaces.place;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/** Driver ends a shift in Hinjewadi and lives in Kharadi (about 30 km by road). */
public class OfferCheckTest {

    private final OfferCheck check = new OfferCheck(HomeRideRules.defaults());

    private OfferCheck.Result evaluate(RideOffer offer) {
        return check.evaluate(HINJEWADI, KHARADI, offer);
    }

    @Test
    public void rideAcrossTownTowardHome_isOffered() {
        OfferCheck.Result r = evaluate(new RideOffer("r", BANER, KOREGAON_PARK, 4, 6));
        assertEquals(OfferCheck.Decision.OFFER, r.decision);
        assertTrue(r.progressMeters > 20_000);
    }

    @Test
    public void rideAwayFromHome_isRejected() {
        OfferCheck.Result r = evaluate(new RideOffer("r", HINJEWADI_2, TALEGAON, 5, 6));
        assertEquals(OfferCheck.Decision.REJECT_NOT_TOWARD_HOME, r.decision);
        assertTrue(r.progressMeters < 0);
    }

    @Test
    public void rideThatOnlyInchesCloser_isRejected() {
        // Ends a few km closer, but less than 25% of the way home.
        Place pimpri = place("pimpri", 18.6298, 73.7997);
        OfferCheck.Result r = evaluate(new RideOffer("r", HINJEWADI, pimpri, 2, 5));
        assertTrue(r.progressMeters > 0);
        assertEquals(OfferCheck.Decision.REJECT_NOT_TOWARD_HOME, r.decision);
    }

    @Test
    public void rideCloserButWithBigDetour_isRejected() {
        // Kothrud is closer to Kharadi, but going via Wakad and south adds too much driving.
        OfferCheck.Result r = evaluate(new RideOffer("r", WAKAD, KOTHRUD, 6, 8));
        assertEquals(OfferCheck.Decision.REJECT_DETOUR_TOO_LONG, r.decision);
        assertTrue(r.detourMeters > check.allowedDetourMeters(30_662));
    }

    @Test
    public void riderWouldWaitLonger_isRejected() {
        OfferCheck.Result r = evaluate(new RideOffer("r", WAKAD, SHIVAJINAGAR, 9, 3));
        assertEquals(OfferCheck.Decision.REJECT_RIDER_WAITS_LONGER, r.decision);
    }

    @Test
    public void riderWaitEqualToOtherDriver_isOffered() {
        OfferCheck.Result r = evaluate(new RideOffer("r", WAKAD, SHIVAJINAGAR, 3, 3));
        assertEquals(OfferCheck.Decision.OFFER, r.decision);
    }

    @Test
    public void noOtherDriverAvailable_isOffered() {
        OfferCheck.Result r = evaluate(new RideOffer("r", AUNDH, VIMAN_NAGAR, 12, RideOffer.NO_OTHER_DRIVER));
        assertEquals(OfferCheck.Decision.OFFER, r.decision);
    }

    @Test
    public void dropNearHome_passesDirectionEvenWithLittleProgress() {
        // Driver already 2.5 km from home; a ride ending 1 km from home barely moves them closer.
        Place driver = place("near", 18.5600, 73.9300);
        Place pickup = place("pickup", 18.5590, 73.9310);
        Place drop = place("drop", 18.5530, 73.9380);
        OfferCheck.Result r = check.evaluate(driver, KHARADI, new RideOffer("r", pickup, drop, 2, 4));
        assertTrue(r.dropToHomeMeters <= 3_000);
        assertEquals(OfferCheck.Decision.OFFER, r.decision);
    }

    @Test
    public void detourAllowance_hasAFloorForShortTripsHome() {
        assertEquals(3_000, check.allowedDetourMeters(5_000));
        assertEquals(9_000, check.allowedDetourMeters(30_000));
    }

    @Test
    public void totalTrip_isPickupPlusRidePlusDriveHome() {
        OfferCheck.Result r = evaluate(new RideOffer("r", BANER, KOREGAON_PARK, 4, 6));
        assertEquals(Geo.roadMeters(HINJEWADI, BANER) + Geo.roadMeters(BANER, KOREGAON_PARK)
                + Geo.roadMeters(KOREGAON_PARK, KHARADI), r.totalTripMeters);
    }
}
