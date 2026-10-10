package com.efast.passenger.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.StrikethroughSpan;
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
import com.efast.passenger.green.Co2Calculator;
import com.efast.passenger.green.EmissionFactors;
import com.efast.passenger.pricing.LaunchOffer;
import com.efast.passenger.pricing.LaunchOfferRules;
import com.efast.passenger.util.EdgeToEdgeHelper;
import com.efast.passenger.util.FareCalculator;
import com.efast.passenger.util.MapRouteHelper;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

public class FareQuoteActivity extends AppCompatActivity {

    public static final String EXTRA_PICKUP_ID = "extra_pickup_id";
    public static final String EXTRA_DROP_ID = "extra_drop_id";

    /* Extras forwarded to FindingDriverActivity and beyond. */
    public static final String EXTRA_DISTANCE_METERS = "extra_distance_meters";
    public static final String EXTRA_BASE_PAISE = "extra_base_paise";
    public static final String EXTRA_GST_PAISE = "extra_gst_paise";
    public static final String EXTRA_TOTAL_PAISE = "extra_total_paise";

    private final RideRepository repo = AppGraph.rideRepository();
    private final Co2Calculator co2 = new Co2Calculator(EmissionFactors.indiaDefaults());
    private final LaunchOfferRules offerRules = LaunchOfferRules.defaults();
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

        binding.co2Label.setText(getString(R.string.co2_fare_estimate,
                Co2Calculator.kg(co2.savedGrams(q.distanceMeters))));
        binding.co2Label.setVisibility(View.VISIBLE);

        showLaunchOffer(LaunchOffer.evaluate(q, offerRules, LocalDate.now()));
    }

    /** Standard fare crossed out, EFast's real fare big below it. Hidden when the rules say no. */
    private void showLaunchOffer(LaunchOffer offer) {
        if (!offer.show) {
            binding.offerBlock.setVisibility(View.GONE);
            return;
        }
        String standard = FareCalculator.rupees(offer.standardTotalPaise);
        SpannableString struck = new SpannableString(getString(R.string.offer_standard_fare, standard));
        struck.setSpan(new StrikethroughSpan(), 0, standard.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        binding.standardFareText.setText(struck);
        binding.offerPriceText.setText(FareCalculator.rupees(offer.actualTotalPaise));
        binding.offerLabelText.setText(getString(R.string.offer_label, offer.percentOff));
        binding.offerNoteText.setText(getString(R.string.offer_note,
                FareCalculator.rupees(offerRules.standardBasePaise),
                FareCalculator.rupees(offerRules.standardRatePaisePerKm),
                offerRules.lastDay.format(DateTimeFormatter.ofPattern("d MMM yyyy", Locale.ENGLISH))));
        binding.offerBlock.setVisibility(View.VISIBLE);
    }
}
