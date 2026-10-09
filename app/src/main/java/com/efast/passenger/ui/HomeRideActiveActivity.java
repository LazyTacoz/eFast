package com.efast.passenger.ui;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.efast.passenger.R;
import com.efast.passenger.data.AppGraph;
import com.efast.passenger.data.Callback;
import com.efast.passenger.data.RideRepository;
import com.efast.passenger.data.model.ChargingBay;
import com.efast.passenger.data.model.HomeAddress;
import com.efast.passenger.data.model.Place;
import com.efast.passenger.data.model.RideOffer;
import com.efast.passenger.data.model.VehicleStatus;
import com.efast.passenger.databinding.ActivityHomeRideActiveBinding;
import com.efast.passenger.databinding.ItemPlaceBinding;
import com.efast.passenger.homeride.BatteryCheck;
import com.efast.passenger.homeride.BusyZonePicker;
import com.efast.passenger.homeride.Geo;
import com.efast.passenger.homeride.HomeRideRules;
import com.efast.passenger.homeride.HomeRideWindow;
import com.efast.passenger.homeride.OfferCheck;
import com.efast.passenger.util.EdgeToEdgeHelper;
import com.efast.passenger.util.FareCalculator;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayDeque;
import java.util.List;

/**
 * Driver side (demo): Home Ride is on. Ride requests come in; each one is checked for
 * direction, detour, rider wait and battery before the driver sees it. Requests that fail
 * are listed with the reason, so a demo shows the rules working (a real driver app would
 * just not show them).
 */
public class HomeRideActiveActivity extends AppCompatActivity {

    public static final String EXTRA_WINDOW_MINUTES = "home_ride_window_minutes";
    public static final String EXTRA_DEMO_LOW_BATTERY = "home_ride_demo_low_battery";

    /** Demo: one window-minute passes every second, so a 60-minute window takes one minute. */
    private static final long DEMO_MS_PER_MINUTE = 1000;
    /** Demo: a new ride request comes in every 5 window-minutes. */
    private static final int MINUTES_BETWEEN_REQUESTS = 5;

    private static final VehicleStatus DEMO_LOW_BATTERY = new VehicleStatus(9, 22_000);
    private static final VehicleStatus DEMO_AFTER_CHARGING = new VehicleStatus(80, 205_000);

    private final RideRepository repo = AppGraph.rideRepository();
    private final HomeRideRules rules = HomeRideRules.defaults();
    private final OfferCheck offerCheck = new OfferCheck(rules);
    private final BatteryCheck batteryCheck = new BatteryCheck(rules);
    private final BusyZonePicker busyZonePicker = new BusyZonePicker(rules);
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final ArrayDeque<RideOffer> incoming = new ArrayDeque<>();

    private ActivityHomeRideActiveBinding binding;
    private HomeRideWindow window;
    private Place home;
    private Place driver;
    private VehicleStatus vehicle;
    private ChargingBay chargingBay;
    private RideOffer currentOffer;
    private int elapsedMinutes = 0;
    private int minutesSinceRequest = 0;
    private boolean matchedYet = false;
    private boolean charging = false;
    private boolean busyZoneShown = false;

    private final Runnable tick = this::onMinutePassed;

    /** The car's battery: from the repository, or the demo "low battery" values. */
    static VehicleStatus vehicleStatus(RideRepository repo, boolean demoLowBattery) {
        return demoLowBattery ? DEMO_LOW_BATTERY : repo.getVehicleStatus();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityHomeRideActiveBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        EdgeToEdgeHelper.applySystemBarPadding(binding.getRoot());

        // Read from the encrypted store here rather than passing the address in the Intent.
        HomeAddress saved = AppGraph.homeAddressStore(this).load();
        if (saved == null) {
            finish();
            return;
        }
        home = saved.asPlace();
        driver = repo.getDriverLocation();
        vehicle = vehicleStatus(repo, getIntent().getBooleanExtra(EXTRA_DEMO_LOW_BATTERY, false));
        window = new HomeRideWindow(
                getIntent().getIntExtra(EXTRA_WINDOW_MINUTES, rules.windowOptionsMinutes[0]), rules);

        binding.headingTo.setText(getString(R.string.home_ride_heading_to, saved.areaName));
        binding.windowProgress.setMax(window.windowMinutes);

        binding.endButton.setOnClickListener(v -> finish());
        binding.skipButton.setOnClickListener(v -> hideOffer());
        binding.acceptButton.setOnClickListener(v -> acceptOffer());
        binding.chargeDoneButton.setOnClickListener(v -> doneCharging());

        // Battery check before anything else: can the car get home at all?
        if (!batteryCheck.canMakeIt(vehicle.rangeMeters, Geo.roadMeters(driver, home))) {
            showChargeFirst();
        }

        repo.getNearbyRideRequests(driver, new Callback<List<RideOffer>>() {
            @Override
            public void onSuccess(List<RideOffer> result) {
                incoming.addAll(result);
            }

            @Override
            public void onError(String message) {
                // Demo: won't happen.
            }
        });

        render();
        handler.postDelayed(tick, DEMO_MS_PER_MINUTE);
    }

    private void onMinutePassed() {
        elapsedMinutes++;
        minutesSinceRequest++;

        if (window.isExpired(elapsedMinutes)) {
            render();
            windowRanOut();
            return;
        }
        if (!charging && currentOffer == null && minutesSinceRequest >= MINUTES_BETWEEN_REQUESTS) {
            minutesSinceRequest = 0;
            handleNextRequest();
        }
        if (!busyZoneShown && !charging && window.shouldSuggestBusyZone(elapsedMinutes, matchedYet)) {
            suggestBusyZone();
        }
        render();
        handler.postDelayed(tick, DEMO_MS_PER_MINUTE);
    }

    private void handleNextRequest() {
        RideOffer offer = incoming.poll();
        if (offer == null) {
            binding.statusText.setText(R.string.home_ride_no_more_requests);
            return;
        }
        OfferCheck.Result result = offerCheck.evaluate(driver, home, offer);
        if (!result.isOffer()) {
            addFiltered(offer, reasonFor(result));
            return;
        }
        // Battery check before every offer.
        if (!batteryCheck.canMakeIt(vehicle.rangeMeters, result.totalTripMeters)) {
            addFiltered(offer, getString(R.string.home_ride_reason_battery));
            showChargeFirst();
            return;
        }
        showOffer(offer, result);
    }

    private String reasonFor(OfferCheck.Result result) {
        switch (result.decision) {
            case REJECT_NOT_TOWARD_HOME:
                return getString(R.string.home_ride_reason_direction);
            case REJECT_DETOUR_TOO_LONG:
                return getString(R.string.home_ride_reason_detour, FareCalculator.km(result.detourMeters));
            case REJECT_RIDER_WAITS_LONGER:
                return getString(R.string.home_ride_reason_rider_wait);
            default:
                return "";
        }
    }

    private void showOffer(RideOffer offer, OfferCheck.Result result) {
        currentOffer = offer;
        matchedYet = true;
        binding.busyZoneCard.setVisibility(View.GONE);
        long fare = FareCalculator.quote(Geo.roadMeters(offer.pickup, offer.drop)).totalPaise;
        binding.offerRoute.setText(getString(R.string.fare_route, offer.pickup.name, offer.drop.name));
        binding.offerDetails.setText(getString(R.string.home_ride_offer_details,
                FareCalculator.km(result.dropToHomeMeters), offer.etaToPickupMinutes, FareCalculator.rupees(fare)));
        binding.offerCard.setVisibility(View.VISIBLE);
    }

    private void hideOffer() {
        currentOffer = null;
        minutesSinceRequest = 0;
        binding.offerCard.setVisibility(View.GONE);
    }

    private void acceptOffer() {
        handler.removeCallbacks(tick);
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.home_ride_accepted_title)
                .setMessage(getString(R.string.home_ride_accepted_message,
                        currentOffer.pickup.name, currentOffer.etaToPickupMinutes))
                .setCancelable(false)
                .setPositiveButton(R.string.action_ok, (d, w) -> finish())
                .show();
    }

    private void windowRanOut() {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.home_ride_expired_title)
                .setMessage(R.string.home_ride_expired_message)
                .setCancelable(false)
                .setPositiveButton(R.string.action_ok, (d, w) -> finish())
                .show();
    }

    private void showChargeFirst() {
        charging = true;
        chargingBay = batteryCheck.pickChargingBay(vehicle.rangeMeters, driver, home, repo.getChargingBays());
        if (chargingBay == null) {
            binding.chargeText.setText(R.string.home_ride_charge_no_bay);
            binding.chargeDoneButton.setVisibility(View.GONE);
        } else {
            long extra = Geo.roadMeters(driver, chargingBay.location)
                    + Geo.roadMeters(chargingBay.location, home) - Geo.roadMeters(driver, home);
            binding.chargeText.setText(getString(R.string.home_ride_charge_needed,
                    chargingBay.location.name, FareCalculator.km(Math.max(0, extra))));
            binding.chargeDoneButton.setVisibility(View.VISIBLE);
        }
        binding.chargeCard.setVisibility(View.VISIBLE);
    }

    /** Demo: pretend the driver drove to the bay and charged. */
    private void doneCharging() {
        driver = chargingBay.location;
        vehicle = DEMO_AFTER_CHARGING;
        charging = false;
        minutesSinceRequest = 0;
        binding.chargeCard.setVisibility(View.GONE);
        render();
    }

    private void suggestBusyZone() {
        busyZoneShown = true;
        Place zone = busyZonePicker.pick(driver, home, repo.getBusyZones());
        if (zone == null) return;
        binding.busyZoneText.setText(getString(R.string.home_ride_busy_zone, zone.name, zone.area));
        binding.busyZoneCard.setVisibility(View.VISIBLE);
    }

    private void addFiltered(RideOffer offer, String reason) {
        ItemPlaceBinding row = ItemPlaceBinding.inflate(getLayoutInflater(), binding.filteredContainer, false);
        row.placeName.setText(getString(R.string.fare_route, offer.pickup.name, offer.drop.name));
        row.placeArea.setText(reason);
        binding.filteredContainer.addView(row.getRoot(), 0);
        binding.filteredLabel.setVisibility(View.VISIBLE);
    }

    private void render() {
        binding.timeLeft.setText(getString(R.string.home_ride_time_left, window.minutesLeft(elapsedMinutes)));
        binding.windowProgress.setProgress(Math.min(elapsedMinutes, window.windowMinutes));
        binding.batteryStatus.setText(getString(R.string.home_ride_battery_status,
                vehicle.batteryPercent, FareCalculator.km(vehicle.rangeMeters)));
    }

    @Override
    protected void onDestroy() {
        handler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }
}
