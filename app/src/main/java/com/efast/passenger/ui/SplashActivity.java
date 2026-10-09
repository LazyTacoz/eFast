package com.efast.passenger.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.efast.passenger.databinding.ActivitySplashBinding;
import com.efast.passenger.util.EdgeToEdgeHelper;

public class SplashActivity extends AppCompatActivity {

    private static final long SPLASH_MS = 1500;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable goToLogin = () -> {
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        ActivitySplashBinding binding = ActivitySplashBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        EdgeToEdgeHelper.applySystemBarPadding(binding.getRoot());
        handler.postDelayed(goToLogin, SPLASH_MS);
    }

    @Override
    protected void onDestroy() {
        handler.removeCallbacks(goToLogin);
        super.onDestroy();
    }
}
