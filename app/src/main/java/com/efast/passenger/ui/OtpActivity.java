package com.efast.passenger.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.efast.passenger.R;
import com.efast.passenger.data.AppGraph;
import com.efast.passenger.data.Callback;
import com.efast.passenger.data.RideRepository;
import com.efast.passenger.databinding.ActivityOtpBinding;
import com.efast.passenger.util.EdgeToEdgeHelper;

public class OtpActivity extends AppCompatActivity {

    private final RideRepository repo = AppGraph.rideRepository();
    private ActivityOtpBinding binding;
    private String phone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityOtpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        EdgeToEdgeHelper.applySystemBarPadding(binding.getRoot());

        phone = getIntent().getStringExtra(LoginActivity.EXTRA_PHONE);
        binding.otpSubtitle.setText(getString(R.string.otp_subtitle, "+91 " + phone));

        binding.verifyButton.setOnClickListener(v -> verify());
        binding.changeNumber.setOnClickListener(v -> finish());
        binding.resendCode.setOnClickListener(v ->
                Toast.makeText(this, R.string.toast_code_resent, Toast.LENGTH_SHORT).show());

        // Auto-verify as soon as the 4th digit is typed.
        binding.otpInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                binding.otpLayout.setError(null);
                if (s.length() == 4) {
                    verify();
                }
            }
        });
    }

    private void verify() {
        String otp = LoginActivity.textOf(binding.otpInput);
        if (otp.length() != 4) {
            binding.otpLayout.setError(getString(R.string.error_otp_length));
            return;
        }
        setLoading(true);
        repo.verifyOtp(phone, otp, new Callback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {
                setLoading(false);
                Intent intent = new Intent(OtpActivity.this, HomeActivity.class);
                // Clear the login screens so Back from Home exits the app.
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }

            @Override
            public void onError(String message) {
                setLoading(false);
                binding.otpLayout.setError(message);
            }
        });
    }

    private void setLoading(boolean loading) {
        binding.verifyButton.setEnabled(!loading);
        binding.progress.setVisibility(loading ? View.VISIBLE : View.GONE);
    }
}
