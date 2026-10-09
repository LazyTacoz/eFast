package com.efast.passenger.homeride;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class HomeRideWindowTest {

    private final HomeRideWindow window = new HomeRideWindow(60, HomeRideRules.defaults());

    @Test
    public void minutesLeft_countsDownAndStopsAtZero() {
        assertEquals(60, window.minutesLeft(0));
        assertEquals(15, window.minutesLeft(45));
        assertEquals(0, window.minutesLeft(75));
    }

    @Test
    public void expiresAtTheEndOfTheWindow() {
        assertFalse(window.isExpired(59));
        assertTrue(window.isExpired(60));
    }

    @Test
    public void busyZone_suggestedAfter15MinutesWithNoMatch() {
        assertFalse(window.shouldSuggestBusyZone(14, false));
        assertTrue(window.shouldSuggestBusyZone(15, false));
        assertFalse(window.shouldSuggestBusyZone(20, true));
        assertFalse(window.shouldSuggestBusyZone(60, false));
    }
}
