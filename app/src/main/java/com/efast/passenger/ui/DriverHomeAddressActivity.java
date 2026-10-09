package com.efast.passenger.ui;

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
import com.efast.passenger.data.model.Place;
import com.efast.passenger.databinding.ActivityDriverHomeAddressBinding;
import com.efast.passenger.databinding.ItemPlaceBinding;
import com.efast.passenger.homeride.AreaMasker;
import com.efast.passenger.homeride.HomeRideLimits;
import com.efast.passenger.homeride.HomeRideRules;
import com.efast.passenger.util.EdgeToEdgeHelper;

import java.security.GeneralSecurityException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Driver side (demo): save the home address once. Changes are capped per month so
 * "home" can't be moved next to the airport to cherry-pick rides.
 * Demo: the area list stands in for real address search and geocoding.
 */
public class DriverHomeAddressActivity extends AppCompatActivity {

    private final RideRepository repo = AppGraph.rideRepository();
    private final HomeRideRules rules = HomeRideRules.defaults();
    private final HomeRideLimits limits = new HomeRideLimits(rules);
    private final List<ItemPlaceBinding> rows = new ArrayList<>();
    private HomeAddressStore homeStore;
    private HomeRideUsageStore usageStore;
    private ActivityDriverHomeAddressBinding binding;
    private HomeAddress existing;
    private Place selected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityDriverHomeAddressBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        EdgeToEdgeHelper.applySystemBarPadding(binding.getRoot());

        homeStore = AppGraph.homeAddressStore(this);
        usageStore = AppGraph.homeRideUsageStore(this);
        existing = homeStore.load();

        List<Place> localities = repo.getLocalities();
        for (Place locality : localities) {
            ItemPlaceBinding row = ItemPlaceBinding.inflate(getLayoutInflater(),
                    binding.localitiesContainer, false);
            row.placeName.setText(locality.name);
            row.placeArea.setText(locality.area);
            row.getRoot().setOnClickListener(v -> select(locality));
            rows.add(row);
            binding.localitiesContainer.addView(row.getRoot());
        }

        if (existing != null) {
            binding.addressInput.setText(existing.addressLine);
            for (Place locality : localities) {
                if (locality.name.equals(existing.areaName)) select(locality);
            }
            binding.limitNote.setText(getString(R.string.home_ride_changes_left,
                    limits.addressChangesLeft(usageStore.addressChanges(), LocalDate.now()),
                    rules.maxAddressChangesPerMonth));
        } else {
            binding.limitNote.setText(R.string.home_address_first_save_free);
        }

        binding.saveButton.setOnClickListener(v -> save());
    }

    private void select(Place locality) {
        selected = locality;
        List<Place> localities = repo.getLocalities();
        for (int i = 0; i < rows.size(); i++) {
            boolean isSelected = localities.get(i).id.equals(locality.id);
            rows.get(i).getRoot().setBackgroundResource(
                    isSelected ? R.color.efast_green_light : android.R.color.transparent);
        }
    }

    private void save() {
        String line = binding.addressInput.getText() == null
                ? "" : binding.addressInput.getText().toString().trim();
        binding.addressLayout.setError(null);
        if (line.isEmpty()) {
            binding.addressLayout.setError(getString(R.string.home_address_error_line));
            return;
        }
        if (selected == null) {
            Toast.makeText(this, R.string.home_address_error_area, Toast.LENGTH_SHORT).show();
            return;
        }

        String area = AreaMasker.generalArea(selected, repo.getLocalities());
        boolean unchanged = existing != null
                && existing.addressLine.equals(line) && existing.areaName.equals(area);
        if (unchanged) {
            finish();
            return;
        }

        LocalDate today = LocalDate.now();
        if (!limits.canSaveAddress(existing != null, usageStore.addressChanges(), today)) {
            Toast.makeText(this, getString(R.string.home_address_error_limit, rules.maxAddressChangesPerMonth),
                    Toast.LENGTH_LONG).show();
            return;
        }

        try {
            homeStore.save(new HomeAddress(line, area, selected.lat, selected.lng));
        } catch (GeneralSecurityException e) {
            Toast.makeText(this, R.string.home_address_error_save, Toast.LENGTH_LONG).show();
            return;
        }
        if (existing != null) {
            usageStore.recordAddressChange(today);
        }
        Toast.makeText(this, R.string.home_address_saved, Toast.LENGTH_SHORT).show();
        finish();
    }
}
