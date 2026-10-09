package com.efast.passenger.homeride;

import com.efast.passenger.data.model.Place;
import com.efast.passenger.data.model.RideOffer;

/**
 * Decides whether a ride request may be offered to a driver who has Home Ride on.
 * Three checks, in order:
 *  1. Direction: the drop is near home, or brings the driver meaningfully closer.
 *  2. Detour: pickup + ride + drive home is not much longer than driving straight home.
 *  3. Rider wait: the rider must not wait longer than with the best regular driver.
 * Battery is checked separately by {@link BatteryCheck}.
 */
public final class OfferCheck {

    public enum Decision {
        OFFER,
        REJECT_NOT_TOWARD_HOME,
        REJECT_DETOUR_TOO_LONG,
        REJECT_RIDER_WAITS_LONGER
    }

    public static final class Result {
        public final Decision decision;
        /** Distance from the drop to home. */
        public final long dropToHomeMeters;
        /** How much closer to home the driver ends up (negative = further away). */
        public final long progressMeters;
        /** Extra driving versus going straight home. */
        public final long detourMeters;
        /** Pickup + ride + drop-to-home, for the battery check. */
        public final long totalTripMeters;

        Result(Decision decision, long dropToHomeMeters, long progressMeters,
               long detourMeters, long totalTripMeters) {
            this.decision = decision;
            this.dropToHomeMeters = dropToHomeMeters;
            this.progressMeters = progressMeters;
            this.detourMeters = detourMeters;
            this.totalTripMeters = totalTripMeters;
        }

        public boolean isOffer() {
            return decision == Decision.OFFER;
        }
    }

    private final HomeRideRules rules;

    public OfferCheck(HomeRideRules rules) {
        this.rules = rules;
    }

    public Result evaluate(Place driver, Place home, RideOffer offer) {
        long directHome = Geo.roadMeters(driver, home);
        long toPickup = Geo.roadMeters(driver, offer.pickup);
        long ride = Geo.roadMeters(offer.pickup, offer.drop);
        long dropToHome = Geo.roadMeters(offer.drop, home);

        long progress = directHome - dropToHome;
        long total = toPickup + ride + dropToHome;
        long detour = total - directHome;

        return new Result(decide(directHome, dropToHome, progress, detour, offer),
                dropToHome, progress, detour, total);
    }

    private Decision decide(long directHome, long dropToHome, long progress, long detour, RideOffer offer) {
        boolean nearHome = dropToHome <= rules.nearHomeMeters;
        long neededProgress = Math.max(rules.minProgressMeters,
                Math.round(directHome * rules.minProgressRatio));
        if (!nearHome && progress < neededProgress) {
            return Decision.REJECT_NOT_TOWARD_HOME;
        }
        if (detour > allowedDetourMeters(directHome)) {
            return Decision.REJECT_DETOUR_TOO_LONG;
        }
        if (offer.bestOtherDriverEtaMinutes != RideOffer.NO_OTHER_DRIVER
                && offer.etaToPickupMinutes > offer.bestOtherDriverEtaMinutes) {
            return Decision.REJECT_RIDER_WAITS_LONGER;
        }
        return Decision.OFFER;
    }

    long allowedDetourMeters(long directHomeMeters) {
        return Math.max(rules.minDetourAllowanceMeters,
                Math.round(directHomeMeters * rules.maxDetourRatio));
    }
}
