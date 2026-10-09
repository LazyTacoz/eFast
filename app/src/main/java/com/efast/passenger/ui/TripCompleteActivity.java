package com.efast.passenger.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.efast.passenger.R;
import com.efast.passenger.data.AppGraph;
import com.efast.passenger.data.RideRepository;
import com.efast.passenger.data.model.Place;
import com.efast.passenger.databinding.ActivityTripCompleteBinding;
import com.efast.passenger.util.EdgeToEdgeHelper;
import com.efast.passenger.util.FareCalculator;

public class TripCompleteActivity extends AppCompatActivity {

    private final RideRepository repo = AppGraph.rideRepository();

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

        binding.payButton.setOnClickListener(v -> {
            Toast.makeText(this, R.string.toast_payment_success, Toast.LENGTH_SHORT).show();
            Intent home = new Intent(this, HomeActivity.class);
            home.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(home);
        });
    }
}
