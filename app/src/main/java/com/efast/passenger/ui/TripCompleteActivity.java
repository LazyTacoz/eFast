package com.efast.passenger.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.efast.passenger.R;
import com.efast.passenger.data.AppGraph;
import com.efast.passenger.data.GreenRideStore;
import com.efast.passenger.data.RideRepository;
import com.efast.passenger.data.model.GreenTrip;
import com.efast.passenger.data.model.Place;
import com.efast.passenger.databinding.ActivityTripCompleteBinding;
import com.efast.passenger.green.Co2Calculator;
import com.efast.passenger.green.EmissionFactors;
import com.efast.passenger.util.EdgeToEdgeHelper;
import com.efast.passenger.util.FareCalculator;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.time.LocalDate;
import java.util.List;

public class TripCompleteActivity extends AppCompatActivity {

    private final RideRepository repo = AppGraph.rideRepository();
    private final Co2Calculator co2 = new Co2Calculator(EmissionFactors.indiaDefaults());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        ActivityTripCompleteBinding binding =
                ActivityTripCompleteBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        EdgeToEdgeHelper.applySystemBarPadding(binding.getRoot());

        Intent data = getIntent();
        String pickupId = data.getStringExtra(FareQuoteActivity.EXTRA_PICKUP_ID);
        String dropId = data.getStringExtra(FareQuoteActivity.EXTRA_DROP_ID);
        Place pickup = repo.getPlaceById(pickupId);
        Place drop = repo.getPlaceById(dropId);
        long distanceMeters = data.getLongExtra(FareQuoteActivity.EXTRA_DISTANCE_METERS, 0);
        long basePaise = data.getLongExtra(FareQuoteActivity.EXTRA_BASE_PAISE, 0);
        long gstPaise = data.getLongExtra(FareQuoteActivity.EXTRA_GST_PAISE, 0);
        long totalPaise = data.getLongExtra(FareQuoteActivity.EXTRA_TOTAL_PAISE, 0);

        binding.routeLabel.setText(getString(R.string.fare_route, pickup.name, drop.name));
        binding.baseFareLabel.setText(getString(R.string.fare_base,
                FareCalculator.km(distanceMeters)));
        binding.baseFareValue.setText(FareCalculator.rupees(basePaise));
        binding.gstValue.setText(FareCalculator.rupees(gstPaise));
        binding.totalValue.setText(FareCalculator.rupees(totalPaise));

        binding.payButton.setText(getString(R.string.action_pay,
                FareCalculator.rupees(totalPaise)));

        showCo2(binding, distanceMeters, savedInstanceState == null);

        binding.payButton.setOnClickListener(v -> {
            Toast.makeText(this, R.string.toast_payment_success, Toast.LENGTH_SHORT).show();
            Intent home = new Intent(this, HomeActivity.class);
            home.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(home);
        });
    }

    /** Records the ride once (not again on screen rotation), then shows this ride's and the running CO2 saving. */
    private void showCo2(ActivityTripCompleteBinding binding, long distanceMeters, boolean firstShow) {
        GreenRideStore store = AppGraph.greenRideStore(this);
        if (firstShow && distanceMeters > 0) {
            store.recordRide(LocalDate.now(), distanceMeters);
        }
        List<GreenTrip> rides = store.rides();
        long totalGrams = 0;
        for (GreenTrip t : rides) totalGrams += co2.savedGrams(t.distanceMeters);

        binding.co2SavedText.setText(getString(R.string.co2_trip_saved,
                Co2Calculator.kg(co2.savedGrams(distanceMeters))));
        binding.co2TotalText.setText(getString(R.string.co2_trip_total, Co2Calculator.kg(totalGrams),
                getResources().getQuantityString(R.plurals.rides_count, rides.size(), rides.size())));

        binding.co2HowButton.setOnClickListener(v -> showCo2Method(this, co2.factors()));
        binding.co2ReportButton.setOnClickListener(v ->
                startActivity(EmissionsReportActivity.personal(this)));
    }

    static void showCo2Method(AppCompatActivity activity, EmissionFactors f) {
        new MaterialAlertDialogBuilder(activity)
                .setTitle(R.string.co2_how_title)
                .setMessage(activity.getString(R.string.co2_how_message, f.evWattHoursPerKm,
                        f.gridGramsCo2PerKwh, f.petrolGramsCo2PerLitre, f.petrolKmPerLitreText()))
                .setPositiveButton(R.string.action_ok, null)
                .show();
    }
}
