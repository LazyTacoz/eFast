package com.efast.passenger.homeride;

import com.efast.passenger.data.model.Place;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.efast.passenger.homeride.PuneTestPlaces.HINJEWADI;
import static com.efast.passenger.homeride.PuneTestPlaces.KHARADI;
import static com.efast.passenger.homeride.PuneTestPlaces.KHARADI_EON;
import static com.efast.passenger.homeride.PuneTestPlaces.PUNE_AIRPORT;
import static com.efast.passenger.homeride.PuneTestPlaces.PUNE_STATION;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

public class BusyZonePickerTest {

    private final BusyZonePicker picker = new BusyZonePicker(HomeRideRules.defaults());
    private final List<Place> launchZones = Arrays.asList(PUNE_AIRPORT, PUNE_STATION, HINJEWADI, KHARADI_EON);

    @Test
    public void picksTheZoneMostOnTheWayHome() {
        // Kharadi EON is next to home (no point waiting there) and Hinjewadi is where the driver
        // already is. The airport adds slightly less driving than Pune Station.
        assertSame(PUNE_AIRPORT, picker.pick(HINJEWADI, KHARADI, launchZones));
        assertSame(PUNE_STATION, picker.pick(HINJEWADI, KHARADI, Arrays.asList(PUNE_STATION, HINJEWADI)));
    }

    @Test
    public void zoneNextToHome_isSkipped() {
        assertNull(picker.pick(HINJEWADI, KHARADI, Collections.singletonList(KHARADI_EON)));
    }

    @Test
    public void zoneFurtherFromHome_isSkipped() {
        assertNull(picker.pick(PUNE_STATION, KHARADI, Collections.singletonList(HINJEWADI)));
    }
}
