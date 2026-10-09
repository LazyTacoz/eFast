package com.efast.passenger.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.efast.passenger.data.AppGraph;
import com.efast.passenger.data.RideRepository;
import com.efast.passenger.data.model.Place;
import com.efast.passenger.databinding.ActivitySearchBinding;
import com.efast.passenger.databinding.ItemPlaceBinding;
import com.efast.passenger.util.EdgeToEdgeHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SearchActivity extends AppCompatActivity {

    private final RideRepository repo = AppGraph.rideRepository();
    private ActivitySearchBinding binding;
    private List<Place> allPlaces;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivitySearchBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        EdgeToEdgeHelper.applySystemBarPadding(binding.getRoot());

        Place pickup = repo.getPlaceById(HomeActivity.DEFAULT_PICKUP_ID);
        if (pickup != null) {
            binding.pickupName.setText(pickup.name);
        }

        allPlaces = repo.getPopularPlaces();
        showPlaces(allPlaces);

        binding.searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }

            @Override
            public void afterTextChanged(Editable s) {
                filter(s.toString());
            }
        });

        binding.searchInput.requestFocus();
    }

    private void filter(String query) {
        String lower = query.toLowerCase(Locale.ROOT).trim();
        if (lower.isEmpty()) {
            showPlaces(allPlaces);
            return;
        }
        List<Place> filtered = new ArrayList<>();
        for (Place p : allPlaces) {
            if (p.name.toLowerCase(Locale.ROOT).contains(lower)
                    || p.area.toLowerCase(Locale.ROOT).contains(lower)) {
                filtered.add(p);
            }
        }
        showPlaces(filtered);
    }

    private void showPlaces(List<Place> places) {
        binding.resultsContainer.removeAllViews();
        binding.noResults.setVisibility(places.isEmpty() ? View.VISIBLE : View.GONE);
        for (Place place : places) {
            ItemPlaceBinding row = ItemPlaceBinding.inflate(getLayoutInflater(),
                    binding.resultsContainer, false);
            row.placeName.setText(place.name);
            row.placeArea.setText(place.area);
            row.getRoot().setOnClickListener(v -> {
                Intent intent = new Intent(this, FareQuoteActivity.class);
                intent.putExtra(FareQuoteActivity.EXTRA_PICKUP_ID, HomeActivity.DEFAULT_PICKUP_ID);
                intent.putExtra(FareQuoteActivity.EXTRA_DROP_ID, place.id);
                startActivity(intent);
            });
            binding.resultsContainer.addView(row.getRoot());
        }
    }
}
