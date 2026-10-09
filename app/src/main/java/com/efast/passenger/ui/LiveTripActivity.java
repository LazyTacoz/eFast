package com.efast.passenger.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.efast.passenger.R;
import com.efast.passenger.data.AppGraph;
import com.efast.passenger.data.RideRepository;
import com.efast.passenger.data.model.Place;
import com.efast.passenger.databinding.ActivityLiveTripBinding;
import com.efast.passenger.util.EdgeToEdgeHelper;
import com.efast.passenger.util.FareCalculator;
import com.efast.passenger.util.MapRouteHelper;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.List;

public class LiveTripActivity extends AppCompatActivity {

    /** Total duration of the simulated trip in milliseconds. */
    private static final long TRIP_DURATION_MS = 20_000;
    /** How often the progress bar updates. */
    private static final long TICK_MS = 500;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final RideRepository repo = AppGraph.rideRepository();
    private ActivityLiveTripBinding binding;
    private long totalDistanceMeters;
    private long elapsed = 0;

    // Map state
    private GoogleMap googleMap;
    private Marker carMarker;
    private List<LatLng> route;

    private final Runnable tick = new Runnable() {
        @Override
        public void run() {
            elapsed += TICK_MS;
            if (elapsed >= TRIP_DURATION_MS) {
                binding.tripProgress.setProgress(100);
                updateCarPosition(1.0);
                goToComplete();
                return;
            }
            int pct = (int) (elapsed * 100 / TRIP_DURATION_MS);
            binding.tripProgress.setProgress(pct);

            long remainingMeters = totalDistanceMeters * (TRIP_DURATION_MS - elapsed) / TRIP_DURATION_MS;
            int remainingSec = (int) ((TRIP_DURATION_MS - elapsed) / 1000);
            int remainingMin = Math.max(1, (remainingSec + 59) / 60);

            binding.remainingLabel.setText(getString(R.string.trip_remaining,
                    FareCalculator.km(remainingMeters)));
            binding.etaLabel.setText(getString(R.string.trip_eta, remainingMin));

            // Move the car marker along the route
            double fraction = (double) elapsed / TRIP_DURATION_MS;
            updateCarPosition(fraction);

            handler.postDelayed(this, TICK_MS);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityLiveTripBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        EdgeToEdgeHelper.applySystemBarPadding(binding.getRoot());

        Intent data = getIntent();
        String pickupId = data.getStringExtra(FareQuoteActivity.EXTRA_PICKUP_ID);
        String dropId = data.getStringExtra(FareQuoteActivity.EXTRA_DROP_ID);
        Place pickup = repo.getPlaceById(pickupId);
        Place drop = repo.getPlaceById(dropId);
        totalDistanceMeters = data.getLongExtra(FareQuoteActivity.EXTRA_DISTANCE_METERS, 0);

        binding.routeLabel.setText(getString(R.string.trip_route, pickup.name, drop.name));
        binding.tripProgress.setMax(100);
        binding.tripProgress.setProgress(0);
        binding.remainingLabel.setText(getString(R.string.trip_remaining,
                FareCalculator.km(totalDistanceMeters)));
        // ETA based on total 20s mapped to minutes proportionally
        binding.etaLabel.setText(getString(R.string.trip_eta, 20));

        // ── Map ──────────────────────────────────────────────────
        SupportMapFragment mapFrag = (SupportMapFragment)
                getSupportFragmentManager().findFragmentById(R.id.tripMap);
        if (mapFrag != null) {
            mapFrag.getMapAsync(map -> setupTripMap(map, pickup, drop));
        }

        // SOS button
        binding.sosButton.setOnClickListener(v ->
                new MaterialAlertDialogBuilder(this)
                        .setTitle(R.string.sos_dialog_title)
                        .setMessage(R.string.sos_dialog_message)
                        .setPositiveButton(R.string.sos_confirm, (d, w) ->
                                Toast.makeText(this, R.string.sos_sent, Toast.LENGTH_SHORT).show())
                        .setNegativeButton(R.string.action_cancel, null)
                        .show());

        // Share trip button
        String vehicle = data.getStringExtra(DriverAssignedActivity.EXTRA_VEHICLE);
        String plate = data.getStringExtra(DriverAssignedActivity.EXTRA_PLATE);
        binding.shareButton.setOnClickListener(v -> {
            String text = getString(R.string.share_trip_text,
                    pickup.name, drop.name, vehicle, plate);
            Intent share = new Intent(Intent.ACTION_SEND);
            share.setType("text/plain");
            share.putExtra(Intent.EXTRA_TEXT, text);
            startActivity(Intent.createChooser(share, null));
        });

        // Start the trip timer
        handler.postDelayed(tick, TICK_MS);
    }

    private void setupTripMap(GoogleMap map, Place pickup, Place drop) {
        this.googleMap = map;
        MapRouteHelper.applyEfastStyle(map);

        LatLng pickupPos = new LatLng(pickup.lat, pickup.lng);
        LatLng dropPos = new LatLng(drop.lat, drop.lng);

        // Draw pickup and drop markers
        MapRouteHelper.addPickupMarker(map, pickupPos, pickup.name);
        MapRouteHelper.addDropMarker(map, dropPos, drop.name);

        // Generate & draw route
        route = MapRouteHelper.generateRoute(pickupPos, dropPos);
        MapRouteHelper.drawRoute(map, route,
                ContextCompat.getColor(this, R.color.efast_green));

        // Add a car marker at the start
        carMarker = map.addMarker(new MarkerOptions()
                .position(pickupPos)
                .title("Your ride")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN))
                .anchor(0.5f, 0.5f)
                .zIndex(10f));

        // Fit camera to route
        binding.getRoot().post(() ->
                MapRouteHelper.fitRoute(map, pickupPos, dropPos, 120));
    }

    private void updateCarPosition(double fraction) {
        if (route == null || carMarker == null || googleMap == null) return;
        LatLng pos = MapRouteHelper.interpolate(route, fraction);
        carMarker.setPosition(pos);
        // Smooth camera follow
        googleMap.animateCamera(CameraUpdateFactory.newLatLng(pos), 400, null);
    }

    private void goToComplete() {
        Intent intent = new Intent(this, TripCompleteActivity.class);
        intent.putExtra(FareQuoteActivity.EXTRA_PICKUP_ID,
                getIntent().getStringExtra(FareQuoteActivity.EXTRA_PICKUP_ID));
        intent.putExtra(FareQuoteActivity.EXTRA_DROP_ID,
                getIntent().getStringExtra(FareQuoteActivity.EXTRA_DROP_ID));
        intent.putExtra(FareQuoteActivity.EXTRA_DISTANCE_METERS,
                getIntent().getLongExtra(FareQuoteActivity.EXTRA_DISTANCE_METERS, 0));
        intent.putExtra(FareQuoteActivity.EXTRA_BASE_PAISE,
                getIntent().getLongExtra(FareQuoteActivity.EXTRA_BASE_PAISE, 0));
        intent.putExtra(FareQuoteActivity.EXTRA_GST_PAISE,
                getIntent().getLongExtra(FareQuoteActivity.EXTRA_GST_PAISE, 0));
        intent.putExtra(FareQuoteActivity.EXTRA_TOTAL_PAISE,
                getIntent().getLongExtra(FareQuoteActivity.EXTRA_TOTAL_PAISE, 0));
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        handler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }
}
