package com.efast.passenger.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.efast.passenger.data.AppGraph;
import com.efast.passenger.data.Callback;
import com.efast.passenger.data.RideRepository;
import com.efast.passenger.data.model.Driver;
import com.efast.passenger.data.model.Place;
import com.efast.passenger.databinding.ActivityFindingDriverBinding;
import com.efast.passenger.util.EdgeToEdgeHelper;

public class FindingDriverActivity extends AppCompatActivity {

    private final RideRepository repo = AppGraph.rideRepository();
    private final Handler handler = new Handler(Looper.getMainLooper());
    private ActivityFindingDriverBinding binding;
    private boolean driverFound = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityFindingDriverBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        EdgeToEdgeHelper.applySystemBarPadding(binding.getRoot());

        String pickupId = getIntent().getStringExtra(FareQuoteActivity.EXTRA_PICKUP_ID);
        Place pickup = repo.getPlaceById(pickupId);

        binding.cancelButton.setOnClickListener(v -> {
            // Go back to Home, clearing the booking screens.
            Intent home = new Intent(this, HomeActivity.class);
            home.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(home);
            finish();
        });

        repo.findDriver(pickup, new Callback<Driver>() {
            @Override
            public void onSuccess(Driver driver) {
                if (isFinishing()) return;
                driverFound = true;
                Intent intent = new Intent(FindingDriverActivity.this, DriverAssignedActivity.class);
                // Forward all fare data
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
                // Driver info
                intent.putExtra(DriverAssignedActivity.EXTRA_DRIVER_NAME, driver.name);
                intent.putExtra(DriverAssignedActivity.EXTRA_VEHICLE, driver.vehicle);
                intent.putExtra(DriverAssignedActivity.EXTRA_PLATE, driver.plate);
                intent.putExtra(DriverAssignedActivity.EXTRA_RATING, driver.rating);
                intent.putExtra(DriverAssignedActivity.EXTRA_ETA, driver.etaMinutes);
                intent.putExtra(DriverAssignedActivity.EXTRA_RIDE_OTP, driver.rideOtp);
                startActivity(intent);
                finish();
            }

            @Override
            public void onError(String message) {
                // Demo — won't happen.
            }
        });
    }

    @Override
    protected void onDestroy() {
        handler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }
}
