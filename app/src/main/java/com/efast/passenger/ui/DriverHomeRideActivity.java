package com.efast.passenger.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.efast.passenger.R;
import com.efast.passenger.data.AppGraph;
import com.efast.passenger.data.HomeAddressStore;
import com.efast.passenger.data.HomeRideUsageStore;
import com.efast.passenger.data.RideRepository;
import com.efast.passenger.data.model.HomeAddress;
import com.efast.passenger.data.model.VehicleStatus;
import com.efast.passenger.databinding.ActivityDriverHomeRideBinding;
import com.efast.passenger.homeride.HomeRideLimits;
import com.efast.passenger.homeride.HomeRideRules;
import com.efast.passenger.util.EdgeToEdgeHelper;
import com.efast.passenger.util.FareCalculator;
import com.google.android.material.button.MaterialButton;

import java.time.LocalDate;

/** Driver side (demo): set home, pick a time window and turn Home Ride on. */
public class DriverHomeRideActivity extends AppCompatActivity {

    private final RideRepository repo = AppGraph.rideRepository();
    private final HomeRideRules rules = HomeRideRules.defaults();
    private final HomeRideLimits limits = new HomeRideLimits(rules);
    private HomeAddressStore homeStore;
    private HomeRideUsageStore usageStore;
    private ActivityDriverHomeRideBinding binding;
    private MaterialButton[] windowButtons;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityDriverHomeRideBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        EdgeToEdgeHelper.applySystemBarPadding(binding.getRoot());

        homeStore = AppGraph.homeAddressStore(this);
        usageStore = AppGraph.homeRideUsageStore(this);

        windowButtons = new MaterialButton[]{binding.window0, binding.window1, binding.window2};
        for (int i = 0; i < windowButtons.length; i++) {
            windowButtons[i].setText(getString(R.string.home_ride_window_option, rules.windowOptionsMinutes[i]));
        }
        binding.windowGroup.check(R.id.window0);

        binding.setHomeButton.setOnClickListener(v ->
                startActivity(new Intent(this, DriverHomeAddressActivity.class)));
        binding.lowBatterySwitch.setOnCheckedChangeListener((b, checked) -> showBattery());
        binding.turnOnButton.setOnClickListener(v -> turnOn());
    }

    @Override
    protected void onResume() {
        super.onResume();
        refresh();
    }

    private void refresh() {
        LocalDate today = LocalDate.now();
        HomeAddress home = homeStore.load();

        // Privacy: only the general area is shown outside the "set home" screen.
        if (home == null) {
            binding.homeArea.setText(R.string.home_ride_home_not_set);
            binding.homeChangesLeft.setText(R.string.home_address_first_save_free);
            binding.setHomeButton.setText(R.string.home_ride_set_home);
        } else {
            binding.homeArea.setText(getString(R.string.home_ride_home_area, home.areaName));
            binding.homeChangesLeft.setText(getString(R.string.home_ride_changes_left,
                    limits.addressChangesLeft(usageStore.addressChanges(), today),
                    rules.maxAddressChangesPerMonth));
            binding.setHomeButton.setText(R.string.home_ride_change_home);
        }

        binding.usesLeft.setText(getString(R.string.home_ride_uses_left,
                limits.usesLeftToday(usageStore.uses(), today), rules.maxUsesPerDay));
        showBattery();
    }

    private void showBattery() {
        VehicleStatus vehicle = HomeRideActiveActivity.vehicleStatus(repo, binding.lowBatterySwitch.isChecked());
        binding.batteryStatus.setText(getString(R.string.home_ride_battery_status,
                vehicle.batteryPercent, FareCalculator.km(vehicle.rangeMeters)));
    }

    private void turnOn() {
        LocalDate today = LocalDate.now();
        if (homeStore.load() == null) {
            Toast.makeText(this, R.string.home_ride_need_home, Toast.LENGTH_SHORT).show();
            return;
        }
        if (limits.usesLeftToday(usageStore.uses(), today) == 0) {
            Toast.makeText(this, getString(R.string.home_ride_no_uses_left, rules.maxUsesPerDay),
                    Toast.LENGTH_LONG).show();
            return;
        }
        usageStore.recordUse(today);

        Intent intent = new Intent(this, HomeRideActiveActivity.class);
        intent.putExtra(HomeRideActiveActivity.EXTRA_WINDOW_MINUTES, selectedWindowMinutes());
        intent.putExtra(HomeRideActiveActivity.EXTRA_DEMO_LOW_BATTERY, binding.lowBatterySwitch.isChecked());
        startActivity(intent);
    }

    private int selectedWindowMinutes() {
        int checked = binding.windowGroup.getCheckedButtonId();
        for (int i = 0; i < windowButtons.length; i++) {
            if (windowButtons[i].getId() == checked) return rules.windowOptionsMinutes[i];
        }
        return rules.windowOptionsMinutes[0];
    }
}
