package com.efast.passenger.homeride;

import org.junit.Test;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class HomeRideLimitsTest {

    private final HomeRideLimits limits = new HomeRideLimits(HomeRideRules.defaults());
    private final LocalDate today = LocalDate.of(2026, 10, 9);

    @Test
    public void twoUsesPerDay() {
        assertEquals(2, limits.usesLeftToday(Collections.emptyList(), today));
        assertEquals(1, limits.usesLeftToday(Collections.singletonList(today), today));
        assertEquals(0, limits.usesLeftToday(Arrays.asList(today, today), today));
        assertEquals(0, limits.usesLeftToday(Arrays.asList(today, today, today), today));
    }

    @Test
    public void yesterdaysUses_dontCount() {
        List<LocalDate> uses = Arrays.asList(today.minusDays(1), today.minusDays(1));
        assertEquals(2, limits.usesLeftToday(uses, today));
    }

    @Test
    public void twoAddressChangesPerCalendarMonth() {
        List<LocalDate> changes = Arrays.asList(LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 5));
        assertEquals(0, limits.addressChangesLeft(changes, today));
        assertFalse(limits.canSaveAddress(true, changes, today));
    }

    @Test
    public void lastMonthsChanges_dontCount() {
        List<LocalDate> changes = Arrays.asList(LocalDate.of(2026, 9, 28), LocalDate.of(2026, 9, 30));
        assertEquals(2, limits.addressChangesLeft(changes, today));
        assertEquals(0, limits.addressChangesLeft(changes, LocalDate.of(2026, 9, 30)));
    }

    @Test
    public void sameMonthLastYear_doesntCount() {
        List<LocalDate> changes = Arrays.asList(LocalDate.of(2025, 10, 2), LocalDate.of(2025, 10, 3));
        assertEquals(2, limits.addressChangesLeft(changes, today));
    }

    @Test
    public void firstSave_isAlwaysAllowed() {
        List<LocalDate> changes = Arrays.asList(today, today);
        assertTrue(limits.canSaveAddress(false, changes, today));
    }
}
