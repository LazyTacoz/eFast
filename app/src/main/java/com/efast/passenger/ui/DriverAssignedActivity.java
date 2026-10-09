package com.efast.passenger.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.efast.passenger.R;
import com.efast.passenger.databinding.ActivityDriverAssignedBinding;
import com.efast.passenger.util.EdgeToEdgeHelper;

import java.util.Locale;

public class DriverAssignedActivity extends AppCompatActivity {

    public static final String EXTRA_DRIVER_NAME = "extra_driver_name";
    public static final String EXTRA_VEHICLE = "extra_vehicle";
    public static final String EXTRA_PLATE = "extra_plate";
    public static final String EXTRA_RATING = "extra_rating";
    public static final String EXTRA_ETA = "extra_eta";
    public static final String EXTRA_RIDE_OTP = "extra_ride_otp";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        ActivityDriverAssignedBinding binding =
                ActivityDriverAssignedBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        EdgeToEdgeHelper.applySystemBarPadding(binding.getRoot());

        Intent data = getIntent();
        String driverName = data.getStringExtra(EXTRA_DRIVER_NAME);
        String vehicle = data.getStringExtra(EXTRA_VEHICLE);
        String plate = data.getStringExtra(EXTRA_PLATE);
        double rating = data.getDoubleExtra(EXTRA_RATING, 0);
        int eta = data.getIntExtra(EXTRA_ETA, 0);
        String rideOtp = data.getStringExtra(EXTRA_RIDE_OTP);

        binding.driverName.setText(driverName);
        binding.driverRating.setText(getString(R.string.driver_rating,
                String.format(Locale.ENGLISH, "%.1f", rating)));
        binding.vehicleInfo.setText(vehicle);
        binding.plateInfo.setText(plate);
        binding.etaLabel.setText(getString(R.string.driver_arriving, eta));
        binding.rideOtp.setText(rideOtp);

        binding.callButton.setOnClickListener(v ->
                Toast.makeText(this, R.string.toast_call_demo, Toast.LENGTH_SHORT).show());

        binding.startTripButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, LiveTripActivity.class);
            // Forward fare data
            intent.putExtra(FareQuoteActivity.EXTRA_PICKUP_ID,
                    data.getStringExtra(FareQuoteActivity.EXTRA_PICKUP_ID));
            intent.putExtra(FareQuoteActivity.EXTRA_DROP_ID,
                    data.getStringExtra(FareQuoteActivity.EXTRA_DROP_ID));
            intent.putExtra(FareQuoteActivity.EXTRA_DISTANCE_METERS,
                    data.getLongExtra(FareQuoteActivity.EXTRA_DISTANCE_METERS, 0));
            intent.putExtra(FareQuoteActivity.EXTRA_BASE_PAISE,
                    data.getLongExtra(FareQuoteActivity.EXTRA_BASE_PAISE, 0));
            intent.putExtra(FareQuoteActivity.EXTRA_GST_PAISE,
                    data.getLongExtra(FareQuoteActivity.EXTRA_GST_PAISE, 0));
            intent.putExtra(FareQuoteActivity.EXTRA_TOTAL_PAISE,
                    data.getLongExtra(FareQuoteActivity.EXTRA_TOTAL_PAISE, 0));
            // Driver info for share
            intent.putExtra(EXTRA_DRIVER_NAME, driverName);
            intent.putExtra(EXTRA_VEHICLE, vehicle);
            intent.putExtra(EXTRA_PLATE, plate);
            startActivity(intent);
            finish();
        });
    }
}
