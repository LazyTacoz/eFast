package com.efast.passenger.homeride;

import com.efast.passenger.data.model.ChargingBay;
import com.efast.passenger.data.model.Place;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

import static com.efast.passenger.homeride.PuneTestPlaces.HINJEWADI;
import static com.efast.passenger.homeride.PuneTestPlaces.KHARADI;
import static com.efast.passenger.homeride.PuneTestPlaces.place;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class BatteryCheckTest {

    private final BatteryCheck check = new BatteryCheck(HomeRideRules.defaults());

    private static final ChargingBay BANER_HUB =
            new ChargingBay("baner", place("hub_baner", 18.5642, 73.7769), 2);
    private static final ChargingBay WAKAD_HUB_FULL =
            new ChargingBay("wakad", place("hub_wakad", 18.5990, 73.7620), 0);
    private static final ChargingBay SHIVAJINAGAR_HUB =
            new ChargingBay("shivajinagar", place("hub_shivajinagar", 18.5300, 73.8530), 1);
    private static final ChargingBay VIMAN_NAGAR_HUB =
            new ChargingBay("viman", place("hub_viman", 18.5650, 73.9100), 3);

    @Test
    public void requiredRange_addsTwentyPercentAndFiveKmReserve() {
        // 30 km x 1.2 + 5 km = 41 km
        assertEquals(41_000, check.requiredRangeMeters(30_000));
        assertEquals(5_000, check.requiredRangeMeters(0));
    }

    @Test
    public void canMakeIt_exactlyAtTheLimit() {
        assertTrue(check.canMakeIt(41_000, 30_000));
        assertFalse(check.canMakeIt(40_999, 30_000));
    }

    @Test
    public void picksFreeReachableBay_withLeastExtraDistance() {
        // 22 km of range: Shivajinagar and Viman Nagar are out of reach with the buffer, Wakad is full.
        ChargingBay bay = check.pickChargingBay(22_000, HINJEWADI, KHARADI,
                Arrays.asList(WAKAD_HUB_FULL, SHIVAJINAGAR_HUB, VIMAN_NAGAR_HUB, BANER_HUB));
        assertSame(BANER_HUB, bay);
    }

    @Test
    public void withMoreRange_prefersTheBayFurtherAlongTheWayHome() {
        ChargingBay bay = check.pickChargingBay(60_000, HINJEWADI, KHARADI,
                Arrays.asList(BANER_HUB, SHIVAJINAGAR_HUB, VIMAN_NAGAR_HUB));
        long extraViman = Geo.roadMeters(HINJEWADI, VIMAN_NAGAR_HUB.location)
                + Geo.roadMeters(VIMAN_NAGAR_HUB.location, KHARADI) - Geo.roadMeters(HINJEWADI, KHARADI);
        long extraChosen = Geo.roadMeters(HINJEWADI, bay.location)
                + Geo.roadMeters(bay.location, KHARADI) - Geo.roadMeters(HINJEWADI, KHARADI);
        assertTrue(extraChosen <= extraViman);
    }

    @Test
    public void fullBay_isNeverPicked() {
        assertNull(check.pickChargingBay(100_000, HINJEWADI, KHARADI,
                Collections.singletonList(WAKAD_HUB_FULL)));
    }

    @Test
    public void noReachableBay_returnsNull() {
        Place farAway = place("far", 18.9000, 74.2000);
        ChargingBay farBay = new ChargingBay("far", farAway, 4);
        assertNull(check.pickChargingBay(10_000, HINJEWADI, KHARADI, Collections.singletonList(farBay)));
    }
}
