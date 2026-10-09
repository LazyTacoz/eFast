package com.efast.passenger.ui;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.efast.passenger.R;
import com.efast.passenger.data.AppGraph;
import com.efast.passenger.data.RideRepository;
import com.efast.passenger.data.model.Place;
import com.efast.passenger.databinding.ActivityHomeBinding;
import com.efast.passenger.databinding.ItemPlaceBinding;
import com.efast.passenger.util.EdgeToEdgeHelper;
import com.efast.passenger.util.MapRouteHelper;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;

import java.util.Calendar;

public class HomeActivity extends AppCompatActivity {

    /** Default pickup for the demo (Viman Nagar). */
    public static final String DEFAULT_PICKUP_ID = "viman_nagar";

    private final RideRepository repo = AppGraph.rideRepository();
    private ActivityHomeBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        EdgeToEdgeHelper.applySystemBarPadding(binding.getRoot());

        binding.greeting.setText(greetingForNow());

        // ── Map ──────────────────────────────────────────────────
        Place defaultPickup = repo.getPlaceById(DEFAULT_PICKUP_ID);
        SupportMapFragment mapFrag = (SupportMapFragment)
                getSupportFragmentManager().findFragmentById(R.id.homeMap);
        if (mapFrag != null && defaultPickup != null) {
            mapFrag.getMapAsync(map -> setupHomeMap(map, defaultPickup));
        }

        binding.whereToCard.setOnClickListener(v -> {
            startActivity(new Intent(this, SearchActivity.class));
        });

        for (Place place : repo.getPopularPlaces()) {
            ItemPlaceBinding row = ItemPlaceBinding.inflate(getLayoutInflater(), binding.placesContainer, false);
            row.placeName.setText(place.name);
            row.placeArea.setText(place.area);
            row.getRoot().setOnClickListener(v -> {
                Intent intent = new Intent(this, FareQuoteActivity.class);
                intent.putExtra(FareQuoteActivity.EXTRA_PICKUP_ID, DEFAULT_PICKUP_ID);
                intent.putExtra(FareQuoteActivity.EXTRA_DROP_ID, place.id);
                startActivity(intent);
            });
            binding.placesContainer.addView(row.getRoot());
        }
    }

    private void setupHomeMap(GoogleMap map, Place pickup) {
        MapRouteHelper.applyEfastStyle(map);
        MapRouteHelper.disableGestures(map);

        LatLng center = new LatLng(pickup.lat, pickup.lng);
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(center, 14.5f));
    }

    private int greetingForNow() {
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        if (hour < 12) return R.string.greeting_morning;
        if (hour < 17) return R.string.greeting_afternoon;
        return R.string.greeting_evening;
    }
}
