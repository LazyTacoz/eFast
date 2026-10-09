package com.efast.passenger.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.efast.passenger.R;
import com.efast.passenger.data.AppGraph;
import com.efast.passenger.data.Callback;
import com.efast.passenger.data.RideRepository;
import com.efast.passenger.data.model.FareQuote;
import com.efast.passenger.data.model.Place;
import com.efast.passenger.databinding.ActivityFareQuoteBinding;
import com.efast.passenger.util.EdgeToEdgeHelper;
import com.efast.passenger.util.FareCalculator;
import com.efast.passenger.util.MapRouteHelper;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;

import java.util.List;

public class FareQuoteActivity extends AppCompatActivity {

    public static final String EXTRA_PICKUP_ID = "extra_pickup_id";
    public static final String EXTRA_DROP_ID = "extra_drop_id";

    /* Extras forwarded to FindingDriverActivity and beyond. */
    public static final String EXTRA_DISTANCE_METERS = "extra_distance_meters";
    public static final String EXTRA_BASE_PAISE = "extra_base_paise";
    public static final String EXTRA_GST_PAISE = "extra_gst_paise";
    public static final String EXTRA_TOTAL_PAISE = "extra_total_paise";

    private final RideRepository repo = AppGraph.rideRepository();
    private ActivityFareQuoteBinding binding;
    private Place pickup;
    private Place drop;
    private FareQuote quote;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityFareQuoteBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        EdgeToEdgeHelper.applySystemBarPadding(binding.getRoot());

        pickup = repo.getPlaceById(getIntent().getStringExtra(EXTRA_PICKUP_ID));
        drop = repo.getPlaceById(getIntent().getStringExtra(EXTRA_DROP_ID));

        binding.routeLabel.setText(getString(R.string.fare_route, pickup.name, drop.name));

        // ── Map ──────────────────────────────────────────────────
        SupportMapFragment mapFrag = (SupportMapFragment)
                getSupportFragmentManager().findFragmentById(R.id.fareMap);
        if (mapFrag != null) {
            mapFrag.getMapAsync(map -> setupRouteMap(map, pickup, drop));
        }

        repo.getQuote(pickup, drop, new Callback<FareQuote>() {
            @Override
            public void onSuccess(FareQuote result) {
                quote = result;
                showQuote(result);
            }

            @Override
            public void onError(String message) {
                // Demo — won't happen.
            }
        });

        binding.bookButton.setOnClickListener(v -> {
            if (quote == null) return;
            Intent intent = new Intent(this, FindingDriverActivity.class);
            intent.putExtra(EXTRA_PICKUP_ID, pickup.id);
            intent.putExtra(EXTRA_DROP_ID, drop.id);
            intent.putExtra(EXTRA_DISTANCE_METERS, quote.distanceMeters);
            intent.putExtra(EXTRA_BASE_PAISE, quote.baseFarePaise);
            intent.putExtra(EXTRA_GST_PAISE, quote.gstPaise);
            intent.putExtra(EXTRA_TOTAL_PAISE, quote.totalPaise);
            startActivity(intent);
        });
    }

    private void setupRouteMap(GoogleMap map, Place from, Place to) {
        MapRouteHelper.applyEfastStyle(map);
        MapRouteHelper.disableGestures(map);

        LatLng pickupPos = new LatLng(from.lat, from.lng);
        LatLng dropPos = new LatLng(to.lat, to.lng);

        MapRouteHelper.addPickupMarker(map, pickupPos, from.name);
        MapRouteHelper.addDropMarker(map, dropPos, to.name);

        List<LatLng> route = MapRouteHelper.generateRoute(pickupPos, dropPos);
        MapRouteHelper.drawRoute(map, route,
                ContextCompat.getColor(this, R.color.efast_green));

        // Fit camera after layout so the map dimensions are known
        binding.getRoot().post(() ->
                MapRouteHelper.fitRoute(map, pickupPos, dropPos, 100));
    }

    private void showQuote(FareQuote q) {
        binding.progress.setVisibility(View.GONE);
        binding.fareCard.setVisibility(View.VISIBLE);
        binding.bookButton.setVisibility(View.VISIBLE);

        binding.distanceLabel.setText(getString(R.string.fare_distance, FareCalculator.km(q.distanceMeters)));
        binding.baseFareLabel.setText(getString(R.string.fare_base, FareCalculator.km(q.distanceMeters)));
        binding.baseFareValue.setText(FareCalculator.rupees(q.baseFarePaise));
        binding.gstValue.setText(FareCalculator.rupees(q.gstPaise));
        binding.totalValue.setText(FareCalculator.rupees(q.totalPaise));
    }
}
