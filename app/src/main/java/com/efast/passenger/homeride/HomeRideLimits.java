package com.efast.passenger.homeride;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

/**
 * Anti-misuse limits: Home Ride uses per day, and home address changes per month.
 * Takes the history as plain dates so it stays easy to test.
 */
public final class HomeRideLimits {

    private final HomeRideRules rules;

    public HomeRideLimits(HomeRideRules rules) {
        this.rules = rules;
    }

    public int usesLeftToday(List<LocalDate> uses, LocalDate today) {
        int used = 0;
        for (LocalDate d : uses) {
            if (d.equals(today)) used++;
        }
        return Math.max(0, rules.maxUsesPerDay - used);
    }

    /** Changes left this calendar month. Saving a home for the first time is not a change. */
    public int addressChangesLeft(List<LocalDate> changes, LocalDate today) {
        YearMonth month = YearMonth.from(today);
        int used = 0;
        for (LocalDate d : changes) {
            if (YearMonth.from(d).equals(month)) used++;
        }
        return Math.max(0, rules.maxAddressChangesPerMonth - used);
    }

    public boolean canSaveAddress(boolean hasSavedHome, List<LocalDate> changes, LocalDate today) {
        return !hasSavedHome || addressChangesLeft(changes, today) > 0;
    }
}
