package com.efast.passenger.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.efast.passenger.R;
import com.efast.passenger.data.AppGraph;
import com.efast.passenger.data.Callback;
import com.efast.passenger.data.RideRepository;
import com.efast.passenger.databinding.ActivityLoginBinding;
import com.efast.passenger.util.EdgeToEdgeHelper;

public class LoginActivity extends AppCompatActivity {

    public static final String EXTRA_PHONE = "extra_phone";

    private final RideRepository repo = AppGraph.rideRepository();
    private ActivityLoginBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        EdgeToEdgeHelper.applySystemBarPadding(binding.getRoot());

        binding.continueButton.setOnClickListener(v -> submit());
        binding.phoneInput.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                submit();
                return true;
            }
            return false;
        });
    }

    private void submit() {
        final String phone = textOf(binding.phoneInput);
        // Indian mobile numbers: 10 digits, starting 6, 7, 8 or 9.
        if (!phone.matches("[6-9]\\d{9}")) {
            binding.phoneLayout.setError(getString(R.string.error_phone));
            return;
        }
        binding.phoneLayout.setError(null);
        setLoading(true);
        repo.requestOtp(phone, new Callback<Void>() {
            @Override
            public void onSuccess(Void result) {
                setLoading(false);
                Intent intent = new Intent(LoginActivity.this, OtpActivity.class);
                intent.putExtra(EXTRA_PHONE, phone);
                startActivity(intent);
            }

            @Override
            public void onError(String message) {
                setLoading(false);
                binding.phoneLayout.setError(message);
            }
        });
    }

    private void setLoading(boolean loading) {
        binding.continueButton.setEnabled(!loading);
        binding.progress.setVisibility(loading ? View.VISIBLE : View.GONE);
    }

    static String textOf(EditText editText) {
        return editText.getText() == null ? "" : editText.getText().toString().trim();
    }
}
