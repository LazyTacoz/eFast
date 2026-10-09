package com.efast.passenger.homeride;

import com.efast.passenger.data.model.HomeAddress;
import com.efast.passenger.data.model.Place;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static com.efast.passenger.homeride.PuneTestPlaces.KHARADI;
import static com.efast.passenger.homeride.PuneTestPlaces.VIMAN_NAGAR;
import static com.efast.passenger.homeride.PuneTestPlaces.place;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class AreaMaskerTest {

    private final List<Place> localities = Arrays.asList(
            new Place("kharadi", "Kharadi", "East Pune", KHARADI.lat, KHARADI.lng),
            new Place("viman_nagar", "Viman Nagar", "East Pune", VIMAN_NAGAR.lat, VIMAN_NAGAR.lng));

    @Test
    public void showsNearestLocalityName() {
        Place homeStreet = place("home", 18.5540, 73.9450);
        assertEquals("Kharadi", AreaMasker.generalArea(homeStreet, localities));
    }

    @Test
    public void farFromEveryLocality_showsOnlyTheCity() {
        Place lonavala = place("lonavala", 18.7500, 73.4000);
        assertEquals("Pune", AreaMasker.generalArea(lonavala, localities));
    }

    @Test
    public void homeAddressToString_neverLeaksTheAddress() {
        HomeAddress home = new HomeAddress("Flat 402, Lane 7, Kharadi", "Kharadi", 18.5540, 73.9450);
        String printed = home.toString();
        assertFalse(printed.contains("402"));
        assertFalse(printed.contains("18.55"));
        assertFalse(printed.contains("73.94"));
    }
}
